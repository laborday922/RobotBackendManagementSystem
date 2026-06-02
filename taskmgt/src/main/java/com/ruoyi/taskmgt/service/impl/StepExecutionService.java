package com.ruoyi.taskmgt.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.enums.ReturnNo;
import com.ruoyi.common.exception.task.TaskmgtException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.robots.domain.Robot;
import com.ruoyi.robots.service.IRobotsService;
import com.ruoyi.taskmgt.constants.TaskLogEventType;
import com.ruoyi.taskmgt.domain.StepRepository;
import com.ruoyi.taskmgt.domain.bo.Task;
import com.ruoyi.taskmgt.domain.bo.TaskStep;
import com.ruoyi.taskmgt.event.StepCompletedEvent;
import com.ruoyi.taskmgt.invoker.RobotInvoker;
import com.ruoyi.taskmgt.invoker.dto.RobotTaskRequest;
import com.ruoyi.taskmgt.invoker.dto.RobotTaskResponse;
import com.ruoyi.taskmgt.loadbalancer.RobotLoadBalancer;
import com.ruoyi.taskmgt.monitor.AsyncOperationMonitor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class StepExecutionService {
    private final StepRepository stepRepository;
    private final TaskLogReuseService taskLogService;
    private final IRobotsService robotService;
    private final ObjectMapper objectMapper;
    private final RobotLoadBalancer robotLoadBalancer;
    private final MessageSourceAccessor messageSourceAccessor;
    private final ApplicationEventPublisher eventPublisher;
    private final AsyncOperationMonitor asyncMonitor;
    private final RobotInvoker robotInvoker;
    private final RedisCache redisCache;

//    @Value("${task.callback.base-url:http://localhost:8080}")
//    private String callbackBaseUrl;

    @Transactional
    public void executeStep(TaskStep step, Task task) {
        Long stepId = step.getId();
        log.info("开始执行步骤: stepId={}, taskId={}, taskStatus={}", stepId, step.getTaskId(), task != null ? task.getStatus() : null);
        try {
            step.setStatus(TaskStep.EXECUTING);
            step.setStartTime(new Date());
            redisCache.deleteObject(stepRepository.update(step));

            taskLogService.record(step.getTaskId(), step.getId(),
                    TaskLogEventType.STEP_START,
                    String.format("步骤[%s]开始执行, 操作ID=%d", step.getStepName(), step.getOperationId()),
                    "system", null);

            Long robotId = resolveRobotId(task, step);
            if (robotId == null) {
                throw new TaskmgtException(ReturnNo.RESOURCE_ID_NOTEXIST,
                        new String[]{messageSourceAccessor.getMessage("机器人", "未知")},
                        messageSourceAccessor.getMessage(ReturnNo.RESOURCE_ID_NOTEXIST.getMessage()));
            }
            sendAndWait(robotId,step);
        } catch (Exception e) {
            log.error("执行步骤失败: stepId={}, taskId={}", stepId, step.getTaskId(), e);
            failStep(step.getId(), e.getMessage());
        }
    }

    public void sendAndWait(Long robotId,TaskStep step){
        Long stepId=step.getId();
        if (!robotInvoker.isRobotOnline(robotId)) {
            System.out.print("机器人不在线");
            throw new TaskmgtException(ReturnNo.ROBOT_OFFLINE,
                    new String[]{robotId.toString()},
                    "机器人不在线");
        }
        try{
            Map<String, Object> params = parseOperationJson(step.getOperationJson());

            RobotTaskRequest request = new RobotTaskRequest();
            String traceId = step.getTraceId() != null ? step.getTraceId() : UUID.randomUUID().toString();
            request.setTraceId(traceId);
            request.setOperationId(step.getOperationId());
            request.setParams(params);
            //request.setCallbackUrl(callbackBaseUrl + "/callback/robot");
            request.setMode(null);

            RobotTaskResponse response = robotInvoker.execute(robotId, request);

            String respMode = response.getMode() != null ? response.getMode() : "SYNC";

            switch (respMode) {
                case "SYNC":
                    if (response.isSuccess()) {
                        log.info("步骤{}同步执行成功", stepId);
                        completeStep(step.getId(), response.getData());
                    } else {
                        log.warn("步骤{}同步执行失败: {}", stepId, response.getErrorMsg());
                        failStep(step.getId(), response.getErrorMsg());
                    }
                    break;
                case "ASYNC":
                    log.info("步骤{}异步执行，注册轮询监控, traceId={}", stepId, response.getTraceId());
                    step.setTraceId(response.getTraceId());
                    step.setStatus(TaskStep.WAITING);
                    stepRepository.update(step);
                    asyncMonitor.registerPolling(step.getId(), response.getTraceId(),
                            step.getOperationId(), robotId, response.getEstimatedFinishTime());
                    taskLogService.record(step.getTaskId(), step.getId(),
                            TaskLogEventType.STEP_ASYNC_SUBMITTED,
                            "异步任务已提交, traceId=" + response.getTraceId(), "system", null);
                    break;
                case "CALLBACK":
                    log.info("步骤{}等待回调, traceId={}", stepId, response.getTraceId());
                    step.setTraceId(response.getTraceId());
                    step.setStatus(TaskStep.WAITING_CALLBACK);
                    stepRepository.update(step);
                    // 注册超时检测（estimatedFinishTime若无默认30分钟）
                    long timeout = response.getEstimatedFinishTime() != null ?
                            response.getEstimatedFinishTime().getTime() - System.currentTimeMillis() :
                            30 * 60 * 1000;
                    asyncMonitor.registerCallbackTimeout(step.getId(), response.getTraceId(), timeout);
                    taskLogService.record(step.getTaskId(), step.getId(),
                            TaskLogEventType.STEP_WAITING_CALLBACK,
                            "等待异步结果, traceId=" + response.getTraceId(), "system", null);
                    break;
                default:
                    log.error("步骤{}未知的结果类型: {}", stepId, respMode);
                    failStep(step.getId(), "未知的结果类型: " + respMode);
        }
        }catch(Exception e){
            log.error("步骤执行异常: {}", stepId, e);
            failStep(step.getId(), e.getMessage());
        }
    }

    private Map<String, Object> parseOperationJson(String json) throws Exception {
        if (StringUtils.isBlank(json)) {
            return new HashMap<>();
        }
        return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
    }

    Long resolveRobotId(Task task, TaskStep step) {
        if (task.getIsGroupTask() == 0) {
            return task.getRobotId();
        }
        if (step.getAssignedRobotId() != null) {
            return step.getAssignedRobotId();
        }
        Long groupId = task.getRobotGroupId();
        if (groupId == null) {
            log.error("组任务缺少 robotGroupId, taskId={}", task.getId());
            throw new TaskmgtException(ReturnNo.DATA_INVALID,
                    new String[]{messageSourceAccessor.getMessage("Task.name"), "组任务未指定机器人组", task.getId().toString()},
                    messageSourceAccessor.getMessage(ReturnNo.DATA_INVALID.getMessage()));
        }
        Robot robot = new Robot();
        robot.setGroupId(groupId);
        robot.setStatus(1);
        robot.setHardwareStatus(0);
        robot.setTaskStatus(2);
        List<Long> availableRobots = robotService.selectRobotsList(robot).stream().map(Robot::getId).toList();
        if (availableRobots.isEmpty()) {
            log.warn("机器人组 {} 无可用机器人, taskId={}", groupId, task.getId());
            throw new TaskmgtException(ReturnNo.SOURCE_IN_USE,
                    new String[]{"机器人组" + groupId + "内无任务可用机器人"},
                    messageSourceAccessor.getMessage(ReturnNo.SOURCE_IN_USE.getMessage()));
        }
        Long selectedRobot = robotLoadBalancer.selectRobot(availableRobots, step);
        if (selectedRobot == null) {
            throw new TaskmgtException(ReturnNo.INTERNAL_SERVER_ERR,
                    new String[]{"负载均衡失败"},
                    messageSourceAccessor.getMessage(ReturnNo.INTERNAL_SERVER_ERR.getMessage()));
        }
        step.setAssignedRobotId(selectedRobot);
        stepRepository.update(step);
        return selectedRobot;
    }

    @Transactional
    public void completeStep(Long stepId, Object resultData) {
        try {
            TaskStep step = stepRepository.findById(stepId)
                    .orElseThrow(() -> new TaskmgtException(ReturnNo.RESOURCE_ID_NOTEXIST,
                            new String[]{messageSourceAccessor.getMessage("Step.name"), stepId.toString()},
                            messageSourceAccessor.getMessage(ReturnNo.RESOURCE_ID_NOTEXIST.getMessage())));
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
            eventPublisher.publishEvent(new StepCompletedEvent(this, step.getTaskId(), stepId, true));
        } catch (OptimisticLockingFailureException e) {
            log.error("步骤 {} 乐观锁冲突，可能已被其他操作修改", stepId, e);
            throw new TaskmgtException(ReturnNo.RESOURCE_FALSIFY,
                    new String[]{}, messageSourceAccessor.getMessage(ReturnNo.RESOURCE_FALSIFY.getMessage()));
        }
    }

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
            eventPublisher.publishEvent(new StepCompletedEvent(this, step.getTaskId(), stepId, false));
        } catch (OptimisticLockingFailureException e) {
            log.error("步骤 {} 乐观锁冲突", stepId, e);
            throw new TaskmgtException(ReturnNo.RESOURCE_FALSIFY,
                    new String[]{}, messageSourceAccessor.getMessage(ReturnNo.RESOURCE_FALSIFY.getMessage()));
        }
    }

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
