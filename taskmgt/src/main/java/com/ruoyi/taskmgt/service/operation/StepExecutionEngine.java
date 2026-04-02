package com.ruoyi.taskmgt.service.operation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.common.enums.ReturnNo;
import com.ruoyi.common.exception.task.TaskmgtException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.taskmgt.common.constants.TaskLogEventType;
import com.ruoyi.taskmgt.domain.StepRepository;
import com.ruoyi.taskmgt.domain.TaskRepository;
import com.ruoyi.taskmgt.domain.bo.Task;
import com.ruoyi.taskmgt.domain.bo.TaskStep;
import com.ruoyi.taskmgt.service.impl.TaskLogReuseService;
import com.ruoyi.taskmgt.service.impl.TaskTriggerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class StepExecutionEngine {

    @Autowired
    private OperationRegistry operationRegistry;
    @Autowired
    private StepRepository stepRepository;
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private TaskLogReuseService taskLogService;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private AsyncOperationMonitor asyncMonitor;

    @Resource
    private TaskTriggerService taskTriggerService;  // 用于触发下一步

    private final MessageSourceAccessor messageSourceAccessor;

    public StepExecutionEngine(MessageSourceAccessor messageSourceAccessor) {
        this.messageSourceAccessor = messageSourceAccessor;
    }

    /**
     * 启动步骤执行 - 由TaskTriggerService调用
     * @param step 要执行的步骤
     * @param task 所属任务（已包含robotId等信息）
     */
    @Transactional
    public void executeStep(TaskStep step, Task task) {
        Long stepId = step.getId();
        log.info("开始执行步骤: {}", stepId);

        try {
            // 1. 更新步骤状态为执行中
            step.setStatus(TaskStep.EXECUTING);
            step.setStartTime(new Date());
            stepRepository.update(step);

            // 2. 记录日志
            taskLogService.record(step.getTaskId(), step.getId(),
                    TaskLogEventType.STEP_START,
                    String.format("步骤[%s]开始执行, 操作ID=%d", step.getStepName(), step.getOperationId()),
                    "system");

            // 3. 解析机器人ID（单任务/组任务）
            Long robotId = resolveRobotId(task, step);
            if (robotId == null) {
                throw new RuntimeException("无法解析执行机器人");
            }

            // 4. 解析operationJson参数
            Map<String, Object> params = parseOperationJson(step.getOperationJson());

            // 5. 获取操作处理器
            OperationHandler handler = operationRegistry.getHandler(step.getOperationId());
            if (handler == null) {
                throw new RuntimeException("未找到操作处理器: " + step.getOperationId());
            }

            // 6. 参数校验
            if (!handler.validateParams(params)) {
                throw new IllegalArgumentException("参数校验失败: " + params);
            }

            // 7. 执行操作
            log.info("步骤{}使用处理器[{}]执行, 机器人={}, 参数={}",
                    stepId, handler.getOperationName(), robotId, params);

            OperationResult result = handler.execute(step, robotId, params);

            // 8. 处理执行结果
            handleResult(step, task, result);

        } catch (Exception e) {
            log.error("步骤执行异常: {}", stepId, e);
            failStep(step, e.getMessage());
        }
    }

    /**
     * 解析operationJson
     */
    private Map<String, Object> parseOperationJson(String json) throws Exception {
        if (StringUtils.isBlank(json)) {
            return new HashMap<>();
        }
        return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
    }

    /**
     * 处理执行结果
     */
    private void handleResult(TaskStep step, Task task, OperationResult result) {
        Long stepId = step.getId();

        switch (result.getType()) {
            case SYNC:
                // 同步结果，立即处理
                if (result.isSuccess()) {
                    log.info("步骤{}同步执行成功", stepId);
                    completeStep(step, result.getData());
                    // 触发下一步
                    taskTriggerService.triggerNextStep(step.getTaskId());
                } else {
                    log.warn("步骤{}同步执行失败: {}", stepId, result.getMessage());
                    failStep(step, result.getMessage());
                }
                break;

            case ASYNC:
                // 异步任务，注册轮询监控
                log.info("步骤{}异步执行，注册监控, traceId={}", stepId, result.getTraceId());
                step.setTraceId(result.getTraceId());
                step.setStatus(TaskStep.WAITING);  // 等待中状态
                stepRepository.update(step);

                // 注册轮询监控
                asyncMonitor.registerPolling(step.getId(), result.getTraceId(),
                        step.getOperationId(), result.getEstimatedFinishTime());

                taskLogService.record(step.getTaskId(), step.getId(),
                        TaskLogEventType.STEP_ASYNC_SUBMITTED,
                        "异步任务已提交, traceId=" + result.getTraceId(), "system");
                break;

            case CALLBACK:
                // 回调模式，只记录traceId等待机器人回调
                log.info("步骤{}等待回调, traceId={}", stepId, result.getTraceId());
                step.setTraceId(result.getTraceId());
                step.setStatus(TaskStep.WAITING_CALLBACK);
                stepRepository.update(step);

                taskLogService.record(step.getTaskId(), step.getId(),
                        TaskLogEventType.STEP_WAITING_CALLBACK,
                        "等待机器人回调, traceId=" + result.getTraceId(), "system");
                break;

            default:
                log.error("步骤{}未知的结果类型: {}", stepId, result.getType());
                failStep(step, "未知的结果类型: " + result.getType());
        }
    }

    /**
     * 完成步骤（由本类或AsyncOperationMonitor调用）
     */
    public void completeStep(TaskStep step, Object resultData) {
        step.setStatus(TaskStep.FINISHED);
        step.setEndTime(new Date());
        step.setResultData(convertToJson(resultData));
        stepRepository.update(step);

        taskLogService.record(step.getTaskId(), step.getId(),
                TaskLogEventType.STEP_COMPLETE, "步骤执行完成", "system");

        log.info("步骤{}已完成", step.getId());
    }

    /**
     * 步骤失败（由本类或AsyncOperationMonitor调用）
     */
    public void failStep(TaskStep step, String errorMsg) {
        step.setStatus(TaskStep.PAUSED);  // 暂停，可重试
        step.setErrorMsg(errorMsg);
        stepRepository.update(step);

        taskLogService.record(step.getTaskId(), step.getId(),
                TaskLogEventType.STEP_FAILED, "执行失败: " + errorMsg, "system");

        log.warn("步骤{}已失败: {}", step.getId(), errorMsg);

        // 任务是否继续？根据业务决定，这里暂停整个任务
        // 也可以只标记步骤失败，让用户手动重试
    }

    /**
     * 解析机器人ID（处理单任务/组任务）
     */
    private Long resolveRobotId(Task task, TaskStep step) {
        // 步骤已指定机器人
        if (step.getAssignedRobotId() != null) {
            return step.getAssignedRobotId();
        }

        // 单任务使用任务绑定的机器人
        if (task.getIsGroupTask() == 0) {
            return task.getRobotId();
        }

        // 组任务：需要实现负载均衡或调度策略
        // 暂时返回null，需要补充robotLoadBalancer
        log.warn("组任务的步骤需要指定机器人或使用负载均衡器");
        return null;
    }

    /**
     * 对象转JSON字符串
     */
    private String convertToJson(Object data) {
        if (data == null) return null;
        if (data instanceof String) return (String) data;
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            log.error("转换结果数据为JSON失败", e);
            return data.toString();
        }
    }

    // ========== 供AsyncOperationMonitor调用的方法 ==========

    /**
     * 异步任务完成回调（由AsyncOperationMonitor调用）
     */
    public void onAsyncComplete(Long stepId, OperationResult result) {
        TaskStep step = stepRepository.findById(stepId).orElse(null);
        if (step == null) {
            log.error("异步完成回调：步骤{}不存在", stepId);
            return;
        }

        if (result.isSuccess()) {
            completeStep(step, result.getData());
            // 触发下一步
            taskTriggerService.triggerNextStep(step.getTaskId());
        } else {
            failStep(step, result.getMessage());
        }
    }

    /**
     * 异步任务超时处理（由AsyncOperationMonitor调用）
     */
    public void onAsyncTimeout(Long stepId) {
        TaskStep step = stepRepository.findById(stepId).orElse(null);
        if (step == null) return;

        failStep(step, "异步任务执行超时");
    }
}