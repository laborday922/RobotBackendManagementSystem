package com.ruoyi.taskmgt.service.impl;

import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.robots.common.RobotsConstants;
import com.ruoyi.robots.domain.Robot;
import com.ruoyi.robots.event.RobotWarningEvent;
import com.ruoyi.robots.service.IRobotWarningsService;
import com.ruoyi.robots.service.IRobotsService;
import com.ruoyi.taskmgt.common.constants.TaskLogEventType;
import com.ruoyi.taskmgt.domain.StepRepository;
import com.ruoyi.taskmgt.domain.TaskRepository;
import com.ruoyi.taskmgt.domain.bo.Task;
import com.ruoyi.taskmgt.domain.bo.TaskStep;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;

@Service
@Slf4j
@Transactional
public class TaskTriggerService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private StepRepository stepRepository;

    @Autowired
    private TaskLogReuseService taskLogService;

    @Autowired
    private RedisCache redisUtil;

    @Autowired
    private IRobotsService robotService;

    @Autowired
    private IRobotWarningsService robotWarningsService;

    /**
     * 每分钟执行一次触发检查
     */
    @Scheduled(cron = "0 * * * * ?")
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
        List<Task> tasks = taskRepository.getTasks(Task.NOTSTART, null, null, null, null, 1, null, null);
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
        List<Task> tasks = taskRepository.getTasks(Task.NOTSTART, null, null, null, null, 2, null, null);
        for (Task task : tasks) {
            Integer battery = robotService.selectRobotsById(task.getRobotId()).getBattery();
            if (battery != null && battery >= task.getBatteryThreshold()) {
                triggerTask(task);
            }
        }
    }

    /**
     * 检查闲时任务触发条件
     */
    private void checkIdleTasks() {
        List<Task> tasks = taskRepository.getTasks(Task.NOTSTART, null, null, null, null, 3, null, null);
        for (Task task : tasks) {
            String taskStatus = robotService.selectRobotsById(task.getRobotId()).getTaskStatus();
            Date idleSince = robotService.selectRobotsById(task.getRobotId()).getIdleStartTime();
            //Date idleSince = getMockIdleSince(task.getRobotId());
            if ("idle".equals(taskStatus) && idleSince != null) {
                long idleMinutes = (System.currentTimeMillis() - idleSince.getTime()) / (60 * 1000);
                if (idleMinutes >= task.getIdleTime()) {
                    triggerTask(task);
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

        // 1. 设置局部顺序 pendingOrder
        if (task.getIsGroupTask() == 0) {
            List<Task> pendingTasks = taskRepository.getTasks(Task.PENDING, 0, null,
                    task.getRobotId(), null, null, null, null);
            int maxOrder = pendingTasks.stream().mapToInt(Task::getPendingOrder).max().orElse(-1);
            task.setPendingOrder(maxOrder + 1);
        } else {
            List<Task> pendingTasks = taskRepository.getTasks(Task.PENDING, 1, null,
                    null, task.getRobotGroupId(), null, null, null);
            int maxOrder = pendingTasks.stream().mapToInt(Task::getPendingOrder).max().orElse(-1);
            task.setPendingOrder(maxOrder + 1);
        }

        // 设置全局顺序 globalPendingOrder,获取当前所有准备中任务的最大全局顺序
        List<Task> allPending = taskRepository.getTasks(Task.PENDING, null, null, null, null, null, null, null);
        int maxGlobal = allPending.stream().mapToInt(Task::getGlobalPendingOrder).max().orElse(-1);
        task.setGlobalPendingOrder(maxGlobal + 1);

        // 风险等级标记
        if (StringUtils.isNotNull(task.getRobotId())) {
            if (robotWarningsService.countUnresolvedByRobotId(task.getRobotId()) != 0)
                task.setRiskLevel(1);
        } else if (StringUtils.isNotNull(task.getRobotGroupId())) {
            Robot robot = new Robot();
            robot.setGroupId(task.getRobotGroupId());
            List<Robot> robots = robotService.selectRobotsList(robot);
            boolean hasWarning = false;
            for (Robot bot : robots) {
                if (robotWarningsService.countUnresolvedByRobotId(bot.getId()) != 0) {
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
                "任务达到触发条件，进入准备队列", "system");
        log.info("任务 {} 已触发进入准备队列", task.getId());
    }

    //任务开始触发
    @Scheduled(fixedDelay = 10000)
    public void checkPendingTasksToStart() {
        log.debug("检查准备中的任务是否可以开始执行");

        // 获取所有准备中任务，已按 globalPendingOrder 排序
        List<Task> pendingTasks = taskRepository.getTasks(Task.PENDING, null, null, null, null, null, null, null);

        for (Task task : pendingTasks) {
            if (task.getIsGroupTask() == 0) {
                // 单任务：检查机器人空闲
                List<Task> executing = taskRepository.getTasks(Task.EXECUTING, 0, null,
                        task.getRobotId(), null, null, null, null);
                if (executing.isEmpty()) {
                    startTask(task);
                }
            } else {
                // 组任务：检查组内所有机器人空闲且无组任务执行
                Long groupId = task.getRobotGroupId();
                List<Task> executingGroup = taskRepository.getTasks(Task.EXECUTING, 1, null,
                        null, groupId, null, null, null);
                if (!executingGroup.isEmpty()) {
                    continue;
                }
                // 检查组内所有机器人是否有执行中的单任务
                Robot robot = new Robot();
                robot.setGroupId(groupId);
                List<Long> robotIds = robotService.selectRobotsList(robot).stream().map(Robot::getId).toList();
                boolean hasExecutingNonGroup = false;
                for (Long rid : robotIds) {
                    List<Task> executingNonGroup = taskRepository.getTasks(Task.EXECUTING, 0, null,
                            rid, null, null, null, null);
                    if (!executingNonGroup.isEmpty()) {
                        hasExecutingNonGroup = true;
                        break;
                    }
                }
                if (!hasExecutingNonGroup) {
                    startTask(task);
                }
            }
        }
    }
    private void startTask(Task task) {
        if (!task.allowTransitStatus(Task.EXECUTING)) {
            log.warn("任务 {} 无法转为执行中", task.getId());
            return;
        }
        task.setStatus(Task.EXECUTING);
        task.setPendingOrder(null);
        task.setGlobalPendingOrder(null);
        task.setUpdateBy("system");
        if(task.getRiskLevel()==1)task.setRiskLevel(2);
        List<String> redisKeys = taskRepository.update(task);
        if (redisKeys != null && !redisKeys.isEmpty()) {
            redisUtil.deleteObject(redisKeys);
        }
        taskLogService.record(task.getId(), null, TaskLogEventType.TASK_START,
                "任务开始执行", "system");
        log.info("任务 {} 开始执行", task.getId());

        // 触发第一个步骤（如果有模板）
        if (task.getTemplateId() != null) {
            List<TaskStep> steps = stepRepository.findStepsByTaskId(task.getId());
            steps.stream()
                    .filter(step -> !Objects.equals(step.getStatus(), TaskStep.FINISHED))
                    .min(Comparator.comparing(TaskStep::getOrderNum))
                    .ifPresent(this::startStep);
        }
    }

    private void startStep(TaskStep step) {
        if (!Objects.equals(step.getStatus(), TaskStep.NOTSTART)&&!Objects.equals(step.getStatus(), TaskStep.PAUSED)) return;
        step.setStatus(TaskStep.EXECUTING);
        step.setStartTime(new Date());
        List<String> redisKeys = stepRepository.update(step);
        if (redisKeys != null && !redisKeys.isEmpty()) {
            redisUtil.deleteObject(redisKeys);
        }
        taskLogService.record(step.getTaskId(), step.getId(), TaskLogEventType.STEP_START,
                "步骤 " + step.getStepName() + " 开始执行", "system");
    }

    /**
     * 处理机器人预警事件，更新相关任务的风险等级
     */
    public void handleRobotWarning(RobotWarningEvent event) {
        Long robotId = event.getRobotId();
        String warningStatus = event.getStatus();       // 0-待处理，1-已解决
        boolean isResolved = RobotsConstants.RESOLVED.equals(warningStatus);
        //处理非组任务
        List<Task> tasks = taskRepository.getTasks(null, 0, null, robotId, null, null, null, null);
        for (Task task : tasks) {
            updateTaskRiskByWarning(task, robotId, isResolved);
        }

        //处理组任务
        Long groupId = robotService.selectRobotsById(robotId).getGroupId();
        if (groupId != null) {
            // 获取该组下所有组任务
            List<Task> groupTasks = taskRepository.getTasks(null, 1, null, null, groupId, null, null, null);
            // 获取该组所有机器人的ID
            Robot robot = new Robot();
            robot.setGroupId(groupId);
            List<Long> robotIds = robotService.selectRobotsList(robot).stream().map(Robot::getId).toList();
            boolean hasUnresolvedWarning = false;
            if (robotWarningsService != null) {
                for (Long rid : robotIds) {
                    if (robotWarningsService.countUnresolvedByRobotId(rid) > 0) {
                        hasUnresolvedWarning = true;
                        break;
                    }
                }
            } else {
                hasUnresolvedWarning = !isResolved || (isResolved && event.isHasRemaining());
            }

            for (Task task : groupTasks) {
                if (hasUnresolvedWarning) {
                    Integer riskLevel = 0;
                    if(Objects.equals(task.getStatus(),Task.EXECUTING)) riskLevel = 2;
                    else if(Objects.equals(task.getStatus(),Task.PAUSED)||Objects.equals(task.getStatus(),Task.PENDING)) riskLevel = 1;
                    task.setRiskLevel(riskLevel);
                    taskLogService.record(task.getId(), null, TaskLogEventType.ROBOT_STATUS_CHANGE,
                            String.format("组内机器人存在未解决预警，任务标记为%s", riskLevel == 2 ? "高风险" : "风险"), "system");
                } else {
                    task.setRiskLevel(0);
                    taskLogService.record(task.getId(), null, TaskLogEventType.ROBOT_STATUS_CHANGE,
                            "组内机器人预警已全部解决，任务风险清除", "system");
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
            // 简单模式：根据事件状态判断
            hasUnresolvedWarning = !isResolved;
        }

        if (hasUnresolvedWarning) {
            int riskLevel = (Objects.equals(task.getStatus(), Task.EXECUTING)) ? 2 : 1;
            task.setRiskLevel(riskLevel);
            taskLogService.record(task.getId(), null, TaskLogEventType.ROBOT_STATUS_CHANGE,
                    String.format("机器人存在未解决预警，任务标记为%s", riskLevel == 2 ? "高风险" : "风险"), "system");
        } else {
            task.setRiskLevel(0);
            taskLogService.record(task.getId(), null, TaskLogEventType.ROBOT_STATUS_CHANGE,
                    "机器人预警已全部解决，任务风险清除", "system");
        }

        task.setUpdateBy("system");
        List<String> redisKeys = taskRepository.update(task);
        if (redisKeys != null && !redisKeys.isEmpty()) {
            redisUtil.deleteObject(redisKeys);
        }
    }

    // 模拟方法（待替换为真实机器人服务调用）
    private Long getMockRobotGroupId(Long robotId) { return 1L; }
    private List<Long> getMockRobotIdsByGroupId(Long groupId) { return Arrays.asList(1L, 2L, 3L); }
    private String getMockRobotStatus(Long robotId) { return "online"; }

    private Integer getMockBattery(Long robotId) { return 80; }
    private Date getMockIdleSince(Long robotId) { return new Date(System.currentTimeMillis() - 10 * 60 * 1000);}
}