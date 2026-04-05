package com.ruoyi.taskmgt.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.common.enums.ReturnNo;
import com.ruoyi.common.exception.task.TaskmgtException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.robots.domain.Robot;
import com.ruoyi.robots.service.IRobotsService;
import com.ruoyi.taskmgt.common.constants.TaskLogEventType;
import com.ruoyi.taskmgt.domain.StepRepository;
import com.ruoyi.taskmgt.domain.TaskRepository;
import com.ruoyi.taskmgt.domain.bo.Task;
import com.ruoyi.taskmgt.domain.bo.TaskStep;
import com.ruoyi.taskmgt.loadbalancer.RobotLoadBalancer;
import com.ruoyi.taskmgt.monitor.AsyncOperationMonitor;
import com.ruoyi.taskmgt.operation.OperationHandler;
import com.ruoyi.taskmgt.operation.OperationRegistry;
import com.ruoyi.taskmgt.service.impl.TaskLogReuseService;
import com.ruoyi.taskmgt.service.trigger.TaskTrigger;
import com.ruoyi.taskmgt.operation.model.OperationResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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
    private IRobotsService robotService;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private AsyncOperationMonitor asyncMonitor;
    @Autowired
    private RobotLoadBalancer robotLoadBalancer;

    private final ConcurrentHashMap<String, Boolean> asyncProcessingMap = new ConcurrentHashMap<>();

    @Resource
    private TaskTrigger taskTrigger;  // 用于触发下一步

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
                    "system", null);

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
            failStep(step.getId(), e.getMessage());
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
                    completeStep(step.getId(), result.getData());
                    // 触发下一步
                    taskTrigger.triggerNextStep(step.getTaskId());
                } else {
                    log.warn("步骤{}同步执行失败: {}", stepId, result.getMessage());
                    failStep(step.getId(), result.getMessage());
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
                        "异步任务已提交, traceId=" + result.getTraceId(), "system", null);
                break;

            case CALLBACK:
                // 回调模式，只记录traceId等待机器人回调
                log.info("步骤{}等待回调, traceId={}", stepId, result.getTraceId());
                step.setTraceId(result.getTraceId());
                step.setStatus(TaskStep.WAITING_CALLBACK);
                stepRepository.update(step);

                taskLogService.record(step.getTaskId(), step.getId(),
                        TaskLogEventType.STEP_WAITING_CALLBACK,
                        "等待机器人回调, traceId=" + result.getTraceId(), "system", null);
                break;

            default:
                log.error("步骤{}未知的结果类型: {}", stepId, result.getType());
                failStep(step.getId(), "未知的结果类型: " + result.getType());
        }
    }

    /**
     * 完成步骤
     * @param stepId 步骤ID
     * @param resultData 结果数据
     */
    @Transactional
    public void completeStep(Long stepId, Object resultData) {
        try {
            // 查询步骤（不带锁，乐观锁由 version 保证）
            TaskStep step = stepRepository.findById(stepId)
                    .orElseThrow(() -> new TaskmgtException(ReturnNo.RESOURCE_ID_NOTEXIST,
                            new String[]{"步骤", stepId.toString()}, "步骤不存在"));
            if (!(Objects.equals(step.getStatus(), TaskStep.EXECUTING) ||
                    Objects.equals(step.getStatus(), TaskStep.WAITING) ||
                    Objects.equals(step.getStatus(), TaskStep.WAITING_CALLBACK))) {
                log.warn("步骤 {} 当前状态 {} 不允许完成", stepId, step.getStatus());
                return;
            }
            step.setStatus(TaskStep.FINISHED);
            step.setEndTime(new Date());
            step.setResultData(convertToJson(resultData));
            stepRepository.update(step);
            taskLogService.record(step.getTaskId(), stepId,
                    TaskLogEventType.STEP_COMPLETE, "步骤执行完成", "system", null);
            log.info("步骤{}已完成", stepId);

        } catch (OptimisticLockingFailureException e) {
            log.error("步骤 {} 乐观锁冲突，可能已被其他操作修改", stepId, e);
            throw new TaskmgtException(ReturnNo.RESOURCE_FALSIFY,
                    new String[]{}, "步骤已被修改，请重试");
        }
    }

    /**
     * 步骤失败
     * @param stepId 步骤ID
     * @param errorMsg 错误信息
     */
    @Transactional
    public void failStep(Long stepId, String errorMsg) {
        try {
            TaskStep step = stepRepository.findById(stepId)
                    .orElseThrow(() -> new TaskmgtException(ReturnNo.RESOURCE_ID_NOTEXIST,
                            new String[]{"步骤", stepId.toString()}, "步骤不存在"));

            if (Objects.equals(step.getStatus(), TaskStep.FINISHED) || Objects.equals(step.getStatus(), TaskStep.TERMINATED)) {
                log.warn("步骤 {} 状态 {} 不允许转为失败", stepId, step.getStatus());
                return;
            }

            step.setStatus(TaskStep.PAUSED);
            step.setErrorMsg(errorMsg);
            stepRepository.update(step);

            taskLogService.record(step.getTaskId(), stepId,
                    TaskLogEventType.STEP_FAILED, "执行失败: " + errorMsg, "system", null);
            log.warn("步骤{}已失败: {}", stepId, errorMsg);

        } catch (OptimisticLockingFailureException e) {
            log.error("步骤 {} 乐观锁冲突", stepId, e);
            throw new TaskmgtException(ReturnNo.RESOURCE_FALSIFY,
                    new String[]{}, "步骤已被修改，请重试");
        }
    }

    /**
     * 异步任务完成回调（防重复处理）
     */
    @Transactional
    public void onAsyncComplete(Long stepId, OperationResult result) {
        String key = "async_" + stepId;
        if (asyncProcessingMap.putIfAbsent(key, Boolean.TRUE) != null) {
            log.warn("重复处理异步完成, stepId={}", stepId);
            return;
        }
        try {
            if (result.isSuccess()) {
                completeStep(stepId, result.getData());
                // 触发下一步
                TaskStep step = stepRepository.findById(stepId).orElse(null);
                if (step != null) {
                    taskTrigger.triggerNextStep(step.getTaskId());
                }
            } else {
                failStep(stepId, result.getMessage());
            }
        } finally {
            asyncProcessingMap.remove(key);
        }
    }

    /**
     * 异步任务超时处理
     */
    @Transactional
    public void onAsyncTimeout(Long stepId) {
        String key = "timeout_" + stepId;
        if (asyncProcessingMap.putIfAbsent(key, Boolean.TRUE) != null) {
            log.warn("重复处理异步超时, stepId={}", stepId);
            return;
        }
        try {
            failStep(stepId, "异步任务执行超时");
        } finally {
            asyncProcessingMap.remove(key);
        }
    }
    /**
     * 解析机器人ID（处理单任务/组任务）
     */
    private Long resolveRobotId(Task task, TaskStep step) {
        // 单任务使用任务绑定的机器人
        if (task.getIsGroupTask() == 0) {
            return task.getRobotId();
        }
        // 步骤已指定机器人
        if (step.getAssignedRobotId() != null) {
            return step.getAssignedRobotId();
        }

        // 组任务使用负载均衡调度策略
        Long groupId = task.getRobotGroupId();
        if (groupId == null) {
            log.error("组任务缺少 robotGroupId, taskId={}", task.getId());
            throw new TaskmgtException(ReturnNo.DATA_INVALID, new Object[]{},"组任务未指定机器人组");
        }
        Robot robot = new Robot();
        robot.setGroupId(groupId);
        robot.setStatus(1);
        robot.setHardwareStatus(0);
        robot.setTaskStatus(2);
        List<Long> availableRobots = robotService.selectRobotsList(robot).stream().map(Robot::getId).toList();
        if (availableRobots.isEmpty()) {
            log.warn("机器人组 {} 无可用机器人, taskId={}", groupId, task.getId());
            throw new TaskmgtException(ReturnNo.SOURCE_IN_USE, new Object[]{},"组内无可用机器人");
        }

        Long selectedRobot = robotLoadBalancer.selectRobot(availableRobots, step);
        if (selectedRobot == null) {

            throw new TaskmgtException(ReturnNo.INTERNAL_SERVER_ERR, new Object[]{},"负载均衡失败");
        }

        step.setAssignedRobotId(selectedRobot);
        stepRepository.update(step);
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
}