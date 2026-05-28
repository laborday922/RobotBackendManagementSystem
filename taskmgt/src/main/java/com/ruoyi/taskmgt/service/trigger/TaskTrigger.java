package com.ruoyi.taskmgt.service.trigger;

import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.robots.common.RobotsConstants;
import com.ruoyi.robots.domain.Robot;
import com.ruoyi.robots.event.RobotWarningEvent;
import com.ruoyi.robots.service.IRobotWarningsService;
import com.ruoyi.robots.service.IRobotsService;
import com.ruoyi.taskmgt.constants.TaskLogEventType;
import com.ruoyi.taskmgt.domain.StepRepository;
import com.ruoyi.taskmgt.domain.TaskRepository;
import com.ruoyi.taskmgt.domain.bo.Task;
import com.ruoyi.taskmgt.domain.bo.TaskStep;
import com.ruoyi.taskmgt.event.ExecuteStepEvent;
import com.ruoyi.taskmgt.event.StepCompletedEvent;
import com.ruoyi.taskmgt.service.impl.TaskLogReuseService;
import com.ruoyi.taskmgt.service.impl.TaskReuseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class TaskTrigger {
    private final TaskRepository taskRepository;
    private final StepRepository stepRepository;
    private final TaskLogReuseService taskLogService;
    private final RedisCache redisUtil;
    private final IRobotsService robotService;
    private final IRobotWarningsService robotWarningsService;
    private final TaskReuseService taskService;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 每5秒执行一次触发检查
     */
    @Scheduled(fixedDelay = 5000)
    public void checkTriggers() {
        log.debug("开始检查任务触发条件");
        checkScheduledTasks();
        checkBatteryTasks();
        checkIdleTasks();
    }

    /**
     * 检查定时任务触发条件
     */
    private void checkScheduledTasks() {
        List<Task> tasks = taskRepository.getTasks(Task.NOTSTART, null, null, null, null, 1, null, null, null);
        Date now = new Date();
        for (Task task : tasks) {
            if (task.getScheduledTime() != null && !task.getScheduledTime().after(now)) {
                triggerTask(task);
            }
        }
    }

    /**
     * 检查电量任务触发条件
     */
    private void checkBatteryTasks() {
        List<Task> tasks = taskRepository.getTasks(Task.NOTSTART, null, null, null, null, 2, null, null, null);
        for (Task task : tasks) {
            Robot robot = robotService.selectRobotsById(task.getRobotId());
            if(StringUtils.isNotNull(robot)){
                Integer battery = robot.getBattery();
                if (battery != null && battery >= task.getBatteryThreshold()) {
                    triggerTask(task);
                }
            }
        }
    }

    /**
     * 检查闲时任务触发条件
     */
    private void checkIdleTasks() {
        List<Task> tasks = taskRepository.getTasks(Task.NOTSTART, null, null, null, null, 3, null, null,null);
        for (Task task : tasks) {
            Robot robot = robotService.selectRobotsById(task.getRobotId());
            if(StringUtils.isNotNull(robot)){
                Integer taskStatus = robot.getTaskStatus();
                Date idleSince = robot.getIdleStartTime();
                if (taskStatus == 2 && idleSince != null) {
                    long idleMinutes = (System.currentTimeMillis() - idleSince.getTime()) / (60 * 1000);
                    if (idleMinutes >= task.getIdleTime()) {
                        triggerTask(task);
                    }
                }
            }
        }
    }

    /**
     * 触发任务：将任务状态从未开始改为准备中，并设置排队顺序
     */
    private void triggerTask(Task task) {
        if (!task.allowTransitStatus(Task.PENDING)) {
            log.warn("任务 {} 当前状态 {} 不允许转为准备中", task.getId(), task.getStatus());
            return;
        }

        // 设置局部顺序 pendingOrder
        if (task.getIsGroupTask() == 0) {
            List<Task> pendingTasks = taskRepository.getTasks(Task.PENDING, 0, null,
                    task.getRobotId(), null, null, null, null, null);
            int maxOrder = pendingTasks.stream().mapToInt(Task::getPendingOrder).max().orElse(-1);
            task.setPendingOrder(maxOrder + 1);
        } else {
            List<Task> pendingTasks = taskRepository.getTasks(Task.PENDING, 1, null,
                    null, task.getRobotGroupId(), null, null, null, null);
            int maxOrder = pendingTasks.stream().mapToInt(Task::getPendingOrder).max().orElse(-1);
            task.setPendingOrder(maxOrder + 1);
        }

        // 设置全局顺序 globalPendingOrder
        List<Task> allPending = taskRepository.getTasks(Task.PENDING, null, null, null, null, null, null, null, null);
        int maxGlobal = allPending.stream().mapToInt(Task::getGlobalPendingOrder).max().orElse(-1);
        task.setGlobalPendingOrder(maxGlobal + 1);

        // 风险等级标记
        if (StringUtils.isNotNull(task.getRobotId())) {
            if (robotWarningsService.countUnresolvedByRobotId(task.getRobotId()) != 0)
                task.setRiskLevel(1);
        } else if (StringUtils.isNotNull(task.getRobotGroupId())) {
            boolean hasWarning = false;
            List<TaskStep> steps = stepRepository.findStepsByTaskId(task.getId());
            Set<Long> involvedRobotIds = new HashSet<>();
            for (TaskStep step : steps) {
                if (step.getAssignedRobotId() != null) {
                    involvedRobotIds.add(step.getAssignedRobotId());
                }
            }
            for (Long robotId:involvedRobotIds){
                if(robotWarningsService.countUnresolvedByRobotId(robotId)!=0){
                    hasWarning = true;
                    break;
                }
            }

            if (hasWarning) task.setRiskLevel(1);
        }

        task.setStatus(Task.PENDING);
        task.setUpdateBy("system");
        List<String> redisKeys = taskRepository.update(task);
        if (redisKeys != null && !redisKeys.isEmpty()) {
            redisUtil.deleteObject(redisKeys);
        }

        taskLogService.record(task.getId(), null, TaskLogEventType.TASK_PENDING,
                "任务达到触发条件，进入准备队列", "system", null);
        log.info("任务 {} 已触发进入准备队列", task.getId());
    }

    /**
     * 检查准备中的任务是否可以开始执行（每4秒执行一次）
     */
    @Scheduled(fixedDelay = 4000)
    public void checkPendingTasksToStart() {
        log.debug("检查准备中的任务是否可以开始执行");

        // 获取所有准备中任务，已按 globalPendingOrder 排序
        List<Task> pendingTasks = taskRepository.getTasks(Task.PENDING, null, null, null, null, null, null, null, null);
        log.debug("准备队列：{}", pendingTasks);
        for (Task task : pendingTasks) {
            if (task.getIsGroupTask() == 0) {
                // 单任务：检查机器人空闲
                List<Task> executing = taskRepository.getTasks(Task.EXECUTING, 0, null,
                        task.getRobotId(), null, null, null, null, null);
                log.debug("任务队列：{}", executing);
                List<TaskStep> executingStep = stepRepository.getSteps(TaskStep.EXECUTING,null,task.getRobotId());
                log.debug("步骤队列：{}", executingStep);
                if (executing.isEmpty()&&executingStep.isEmpty()) {
                    startTask(task);
                }
            } else {
                // 组任务：检查组内所有机器人空闲且无组任务执行
                Long groupId = task.getRobotGroupId();
                List<Task> executingGroup = taskRepository.getTasks(Task.EXECUTING, 1, null,
                        null, groupId, null, null, null, null);
                if (!executingGroup.isEmpty()) {
                    continue;
                }
                // 检查组内是否有至少一个机器人处于空闲状态
                Robot robot = new Robot();
                robot.setGroupId(groupId);
                robot.setStatus(1);
                robot.setHardwareStatus(0);
                robot.setTaskStatus(2);
                List<Long> robotIds = robotService.selectRobotsList(robot).stream().map(Robot::getId).toList();
                boolean hasIdleRobot = robotIds.stream().anyMatch(rid ->
                        taskRepository.getTasks(Task.EXECUTING, null, null, rid, null, null, null, null, null).isEmpty()
                );
                if (hasIdleRobot) {
                    startTask(task);
                }
            }
        }
    }

    /**
     * 开始执行任务：状态变为执行中，并触发第一个步骤
     */
    private void startTask(Task task) {
        if (!task.allowTransitStatus(Task.EXECUTING)) {
            log.warn("任务 {} 无法转为执行中", task.getId());
            return;
        }

        // 更新任务状态
        task.setStatus(Task.EXECUTING);
        task.setPendingOrder(null);
        task.setGlobalPendingOrder(null);
        task.setUpdateBy("system");
        if(task.getRiskLevel() == 1) task.setRiskLevel(2);

        List<String> redisKeys = taskRepository.update(task);
        if (redisKeys != null && !redisKeys.isEmpty()) {
            redisUtil.deleteObject(redisKeys);
        }

        taskLogService.record(task.getId(), null, TaskLogEventType.TASK_START,
                "任务开始执行", "system", null);
        log.info("任务 {} 开始执行", task.getId());

        // 触发第一个步骤
        triggerFirstStep(task);
    }

    /**
     * 触发任务的第一个步骤
     */
    private void triggerFirstStep(Task task) {
        if (task.getTemplateId() == null) {
            log.info("任务 {} 无模板，无需执行步骤", task.getId());
            taskService.completeTask(task);
            return;
        }

        // 查询步骤列表
        List<TaskStep> steps = stepRepository.findStepsByTaskId(task.getId());

        // 找到第一个未完成的步骤
        Optional<TaskStep> firstStep = steps.stream()
                .filter(step -> !Objects.equals(step.getStatus(), TaskStep.FINISHED))
                .min(Comparator.comparing(TaskStep::getOrderNum));

        if (firstStep.isPresent()) {
            TaskStep step = firstStep.get();
            log.info("任务 {} 开始执行第一个步骤: {}", task.getId(), step.getId());

            eventPublisher.publishEvent(new ExecuteStepEvent(this, step, task));
        } else {
            log.info("任务 {} 所有步骤已完成", task.getId());
            taskService.completeTask(task);
        }
    }

    @EventListener
    public void onStepCompleted(StepCompletedEvent event) {
        Long taskId = event.getTaskId();
        Task task = taskRepository.findById(taskId).orElse(null);
        if (task == null || !Objects.equals(task.getStatus(), Task.EXECUTING)) {
            log.warn("任务 {} 不存在或不在执行中，无法触发下一步", taskId);
            return;
        }
        if (!event.isSuccess()) {
            log.error("步骤 {} 执行失败，任务 {} 停止", event.getStepId(), event.getTaskId());
            task.setStatus(Task.PAUSED);
            task.setRiskLevel(2);
            List<String> redisKeys = taskRepository.update(task);
            if (redisKeys != null && !redisKeys.isEmpty()) {
                redisUtil.deleteObject(redisKeys);
            }
            return;
        }
        List<TaskStep> steps = stepRepository.findStepsByTaskId(taskId);

        // 找到下一个未完成的步骤
        Optional<TaskStep> nextStep = steps.stream()
                .filter(step -> !Objects.equals(step.getStatus(), TaskStep.FINISHED)
                        && !Objects.equals(step.getStatus(), TaskStep.EXECUTING))
                .min(Comparator.comparing(TaskStep::getOrderNum));

        if (nextStep.isPresent()) {
            TaskStep step = nextStep.get();
            log.info("任务 {} 触发下一步骤: {}", taskId, step.getId());
            eventPublisher.publishEvent(new ExecuteStepEvent(this, step, task));
        } else {
            log.info("任务 {} 所有步骤执行完毕", taskId);
            taskService.completeTask(task);
        }
    }
    /**
     * 处理机器人预警事件，更新相关任务的风险等级
     */
    public void handleRobotWarning(RobotWarningEvent event) {
        Long robotId = event.getRobotId();
        String warningStatus = event.getStatus();
        boolean isResolved = RobotsConstants.RESOLVED.equals(warningStatus);

        // 处理非组任务
        List<Task> tasks = taskRepository.getTasks(null, 0, null, robotId, null, null, null, null, null);
        for (Task task : tasks) {
            updateTaskRiskByWarning(task, robotId, isResolved);
        }

        // 处理组任务
        Long groupId = robotService.selectRobotsById(robotId).getGroupId();
        if (groupId != null) {
            List<Task> groupTasks = taskRepository.getTasks(null, 1, null, null, groupId, null, null, null, null);
            for (Task task : groupTasks) {
                boolean hasUnresolvedWarning = false;
                boolean isAssigned = stepRepository.countByAssignedRobotIdAndTaskIdAndStatusIn(robotId,task.getId(),List.of(TaskStep.EXECUTING, TaskStep.WAITING, TaskStep.WAITING_CALLBACK))!=0;
                if(isAssigned) {
                    if (robotWarningsService != null) {
                        if (robotWarningsService.countUnresolvedByRobotId(robotId) != 0) {
                            hasUnresolvedWarning = true;
                        }
                    }
                    else {
                        hasUnresolvedWarning = !isResolved || (isResolved && event.isHasRemaining());
                    }
                }
                if (hasUnresolvedWarning) {
                    Integer riskLevel = 0;
                    if(Objects.equals(task.getStatus(),Task.EXECUTING)) riskLevel = 2;
                    else if(Objects.equals(task.getStatus(),Task.PAUSED)||Objects.equals(task.getStatus(),Task.PENDING)) riskLevel = 1;
                    task.setRiskLevel(riskLevel);
                    taskLogService.record(task.getId(), null, TaskLogEventType.ROBOT_STATUS_CHANGE,
                            String.format("组内机器人存在未解决预警，任务标记为%s", riskLevel == 2 ? "高风险" : "风险"), "system", null);
                } else {
                    task.setRiskLevel(0);
                    taskLogService.record(task.getId(), null, TaskLogEventType.ROBOT_STATUS_CHANGE,
                            "组内机器人预警已全部解决，任务风险清除", "system", null);
                }
                task.setUpdateBy("system");
                List<String> redisKeys = taskRepository.update(task);
                if (redisKeys != null && !redisKeys.isEmpty()) {
                    redisUtil.deleteObject(redisKeys);
                }
            }
        }
    }

    /**
     * 根据预警情况更新单个非组任务的风险等级
     */
    private void updateTaskRiskByWarning(Task task, Long robotId, boolean isResolved) {
        if (Objects.equals(task.getStatus(), Task.DISABLED) ||
                Objects.equals(task.getStatus(), Task.TERMINATED) ||
                Objects.equals(task.getStatus(), Task.FINISHED) ||
                Objects.equals(task.getStatus(), Task.DELETED)) {
            return;
        }

        boolean hasUnresolvedWarning = false;
        if (robotWarningsService != null) {
            hasUnresolvedWarning = robotWarningsService.countUnresolvedByRobotId(robotId) > 0;
        } else {
            hasUnresolvedWarning = !isResolved;
        }

        if (hasUnresolvedWarning) {
            int riskLevel = (Objects.equals(task.getStatus(), Task.EXECUTING)) ? 2 : 1;
            task.setRiskLevel(riskLevel);
            taskLogService.record(task.getId(), null, TaskLogEventType.ROBOT_STATUS_CHANGE,
                    String.format("机器人存在未解决预警，任务标记为%s", riskLevel == 2 ? "高风险" : "风险"), "system", null);
        } else {
            task.setRiskLevel(0);
            taskLogService.record(task.getId(), null, TaskLogEventType.ROBOT_STATUS_CHANGE,
                    "机器人预警已全部解决，任务风险清除", "system", null);
        }

        task.setUpdateBy("system");
        List<String> redisKeys = taskRepository.update(task);
        if (redisKeys != null && !redisKeys.isEmpty()) {
            redisUtil.deleteObject(redisKeys);
        }
    }
}
