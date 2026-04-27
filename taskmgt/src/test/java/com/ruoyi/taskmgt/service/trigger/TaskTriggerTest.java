package com.ruoyi.taskmgt.service.trigger;

import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.robots.common.RobotsConstants;
import com.ruoyi.robots.domain.Robot;
import com.ruoyi.robots.event.RobotWarningEvent;
import com.ruoyi.robots.service.IRobotWarningsService;
import com.ruoyi.robots.service.IRobotsService;
import com.ruoyi.taskmgt.TestApplication;
import com.ruoyi.taskmgt.constants.TaskLogEventType;
import com.ruoyi.taskmgt.domain.StepRepository;
import com.ruoyi.taskmgt.domain.TaskRepository;
import com.ruoyi.taskmgt.domain.bo.Task;
import com.ruoyi.taskmgt.domain.bo.TaskStep;
import com.ruoyi.taskmgt.event.ExecuteStepEvent;
import com.ruoyi.taskmgt.event.StepCompletedEvent;
import com.ruoyi.taskmgt.service.impl.TaskLogReuseService;
import com.ruoyi.taskmgt.service.impl.TaskReuseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskTriggerTest {

    @Mock
    private TaskRepository taskRepository;
    @Mock
    private StepRepository stepRepository;
    @Mock
    private TaskLogReuseService taskLogService;
    @Mock
    private RedisCache redisUtil;
    @Mock
    private IRobotsService robotService;
    @Mock
    private IRobotWarningsService robotWarningsService;
    @Mock
    private TaskReuseService taskService;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private TaskTrigger taskTrigger;

    private static final Long TASK_ID = 1L;
    private static final Long ROBOT_ID = 100L;
    private static final Long GROUP_ID = 200L;

    @BeforeEach
    void setUp() {
        // MockitoExtension 会自动初始化，不需要额外设置
    }

    // ==================== 1. 定时任务触发测试 ====================

    @Test
    void testCheckTriggers_ScheduledTaskTriggered() {
        // 准备：一个已到达触发时间的定时任务
        Task task = createTask(TASK_ID, Task.NOTSTART, 0, null, null, 1);
        task.setScheduledTime(new Date(System.currentTimeMillis() - 1000)); // 已过期

        when(taskRepository.getTasks(Task.NOTSTART, null, null, null, null, 1, null, null, null))
                .thenReturn(Collections.singletonList(task));
        when(taskRepository.getTasks(Task.NOTSTART, null, null, null, null, 2, null, null, null))
                .thenReturn(Collections.emptyList());
        when(taskRepository.getTasks(Task.NOTSTART, null, null, null, null, 3, null, null, null))
                .thenReturn(Collections.emptyList());

        // Mock triggerTask 内部依赖
        when(taskRepository.getTasks(Task.PENDING, 0, null, null, null, null, null, null, null))
                .thenReturn(Collections.emptyList());
        when(taskRepository.getTasks(Task.PENDING, null, null, null, null, null, null, null, null))
                .thenReturn(Collections.emptyList());
        when(taskRepository.update(any(Task.class))).thenReturn(Collections.emptyList());

        taskTrigger.checkTriggers();

        verify(taskRepository).update(any(Task.class));
        verify(taskLogService).record(eq(TASK_ID), isNull(), eq(TaskLogEventType.TASK_PENDING),
                contains("触发条件"), eq("system"), isNull());
    }

    @Test
    void testCheckTriggers_BatteryTaskTriggered() {
        // 准备：电量任务 + 机器人电量达标
        Task task = createTask(TASK_ID, Task.NOTSTART, 0, ROBOT_ID, null, 2);
        task.setBatteryThreshold(80);

        Robot robot = new Robot();
        robot.setId(ROBOT_ID);
        robot.setBattery(85);

        when(taskRepository.getTasks(Task.NOTSTART, null, null, null, null, 1, null, null, null))
                .thenReturn(Collections.emptyList());
        when(taskRepository.getTasks(Task.NOTSTART, null, null, null, null, 2, null, null, null))
                .thenReturn(Collections.singletonList(task));
        when(taskRepository.getTasks(Task.NOTSTART, null, null, null, null, 3, null, null, null))
                .thenReturn(Collections.emptyList());

        when(robotService.selectRobotsById(ROBOT_ID)).thenReturn(robot);
        when(taskRepository.getTasks(Task.PENDING, 0, null, ROBOT_ID, null, null, null, null, null))
                .thenReturn(Collections.emptyList());
        when(taskRepository.getTasks(Task.PENDING, null, null, null, null, null, null, null, null))
                .thenReturn(Collections.emptyList());
        when(taskRepository.update(any(Task.class))).thenReturn(Collections.emptyList());

        taskTrigger.checkTriggers();

        verify(taskRepository).update(any(Task.class));
        verify(taskLogService).record(eq(TASK_ID), isNull(), eq(TaskLogEventType.TASK_PENDING),
                anyString(), eq("system"), isNull());
    }

    @Test
    void testCheckTriggers_BatteryTaskNotTriggered() {
        // 准备：电量任务 + 机器人电量不足
        Task task = createTask(TASK_ID, Task.NOTSTART, 0, ROBOT_ID, null, 2);
        task.setBatteryThreshold(90);

        Robot robot = new Robot();
        robot.setId(ROBOT_ID);
        robot.setBattery(80);

        when(taskRepository.getTasks(Task.NOTSTART, null, null, null, null, 1, null, null, null))
                .thenReturn(Collections.emptyList());
        when(taskRepository.getTasks(Task.NOTSTART, null, null, null, null, 2, null, null, null))
                .thenReturn(Collections.singletonList(task));
        when(taskRepository.getTasks(Task.NOTSTART, null, null, null, null, 3, null, null, null))
                .thenReturn(Collections.emptyList());

        when(robotService.selectRobotsById(ROBOT_ID)).thenReturn(robot);

        taskTrigger.checkTriggers();

        verify(taskRepository, never()).update(any(Task.class));
    }

    @Test
    void testCheckTriggers_IdleTaskTriggered() {
        // 准备：闲时任务 + 机器人空闲时间达标
        Task task = createTask(TASK_ID, Task.NOTSTART, 0, ROBOT_ID, null, 3);
        task.setIdleTime(10); // 需要空闲10分钟

        Robot robot = new Robot();
        robot.setId(ROBOT_ID);
        robot.setTaskStatus(2); // 空闲状态
        robot.setIdleStartTime(new Date(System.currentTimeMillis() - 15 * 60 * 1000)); // 空闲15分钟

        when(taskRepository.getTasks(Task.NOTSTART, null, null, null, null, 1, null, null, null))
                .thenReturn(Collections.emptyList());
        when(taskRepository.getTasks(Task.NOTSTART, null, null, null, null, 2, null, null, null))
                .thenReturn(Collections.emptyList());
        when(taskRepository.getTasks(Task.NOTSTART, null, null, null, null, 3, null, null, null))
                .thenReturn(Collections.singletonList(task));

        when(robotService.selectRobotsById(ROBOT_ID)).thenReturn(robot);
        when(taskRepository.getTasks(Task.PENDING, 0, null, ROBOT_ID, null, null, null, null, null))
                .thenReturn(Collections.emptyList());
        when(taskRepository.getTasks(Task.PENDING, null, null, null, null, null, null, null, null))
                .thenReturn(Collections.emptyList());
        when(taskRepository.update(any(Task.class))).thenReturn(Collections.emptyList());

        taskTrigger.checkTriggers();

        verify(taskRepository).update(any(Task.class));
    }

    @Test
    void testCheckTriggers_IdleTaskNotTriggered_RobotBusy() {
        // 准备：闲时任务 + 机器人忙碌
        Task task = createTask(TASK_ID, Task.NOTSTART, 0, ROBOT_ID, null, 3);

        Robot robot = new Robot();
        robot.setId(ROBOT_ID);
        robot.setTaskStatus(1); // 忙碌状态

        when(taskRepository.getTasks(Task.NOTSTART, null, null, null, null, 1, null, null, null))
                .thenReturn(Collections.emptyList());
        when(taskRepository.getTasks(Task.NOTSTART, null, null, null, null, 2, null, null, null))
                .thenReturn(Collections.emptyList());
        when(taskRepository.getTasks(Task.NOTSTART, null, null, null, null, 3, null, null, null))
                .thenReturn(Collections.singletonList(task));

        when(robotService.selectRobotsById(ROBOT_ID)).thenReturn(robot);

        taskTrigger.checkTriggers();

        verify(taskRepository, never()).update(any(Task.class));
    }

    // ==================== 2. 准备任务开始执行测试 ====================

    @Test
    void testCheckPendingTasksToStart_SingleTask() {
        // 准备：单任务 + 机器人空闲
        Task task = createTask(TASK_ID, Task.PENDING, 0, ROBOT_ID, null, null);
        task.setTemplateId(1L);

        when(taskRepository.getTasks(Task.PENDING, null, null, null, null, null, null, null, null))
                .thenReturn(Collections.singletonList(task));
        when(taskRepository.getTasks(Task.EXECUTING, 0, null, ROBOT_ID, null, null, null, null, null))
                .thenReturn(Collections.emptyList());
        when(stepRepository.getSteps(TaskStep.EXECUTING, null, ROBOT_ID))
                .thenReturn(Collections.emptyList());
        when(taskRepository.update(any(Task.class))).thenReturn(Collections.emptyList());

        // 步骤查询
        TaskStep step = createStep(1L, TASK_ID, 1, TaskStep.WAITING);
        when(stepRepository.findStepsByTaskId(TASK_ID)).thenReturn(Collections.singletonList(step));

        taskTrigger.checkPendingTasksToStart();

        verify(taskRepository).update(any(Task.class));
        verify(taskLogService).record(eq(TASK_ID), isNull(), eq(TaskLogEventType.TASK_START),
                anyString(), eq("system"), isNull());
        verify(eventPublisher).publishEvent(any(ExecuteStepEvent.class));
    }

    @Test
    void testCheckPendingTasksToStart_SingleTask_RobotBusy() {
        // 准备：单任务 + 机器人正在执行其他任务
        Task task = createTask(TASK_ID, Task.PENDING, 0, ROBOT_ID, null, null);

        Task executingTask = createTask(2L, Task.EXECUTING, 0, ROBOT_ID, null, null);

        when(taskRepository.getTasks(Task.PENDING, null, null, null, null, null, null, null, null))
                .thenReturn(Collections.singletonList(task));
        when(taskRepository.getTasks(Task.EXECUTING, 0, null, ROBOT_ID, null, null, null, null, null))
                .thenReturn(Collections.singletonList(executingTask));

        taskTrigger.checkPendingTasksToStart();

        verify(taskRepository, never()).update(any(Task.class));
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void testCheckPendingTasksToStart_GroupTask() {
        // 准备：组任务 + 组内机器人空闲
        Task task = createTask(TASK_ID, Task.PENDING, 1, null, GROUP_ID, null);
        task.setTemplateId(1L);

        Robot idleRobot = new Robot();
        idleRobot.setId(ROBOT_ID);
        idleRobot.setGroupId(GROUP_ID);
        idleRobot.setStatus(1);
        idleRobot.setHardwareStatus(0);
        idleRobot.setTaskStatus(2);

        when(taskRepository.getTasks(Task.PENDING, null, null, null, null, null, null, null, null))
                .thenReturn(Collections.singletonList(task));
        when(taskRepository.getTasks(Task.EXECUTING, 1, null, null, GROUP_ID, null, null, null, null))
                .thenReturn(Collections.emptyList());

        Robot queryRobot = new Robot();
        queryRobot.setGroupId(GROUP_ID);
        queryRobot.setStatus(1);
        queryRobot.setHardwareStatus(0);
        queryRobot.setTaskStatus(2);
        when(robotService.selectRobotsList(any(Robot.class))).thenReturn(Collections.singletonList(idleRobot));
        when(taskRepository.getTasks(Task.EXECUTING, null, null, ROBOT_ID, null, null, null, null, null))
                .thenReturn(Collections.emptyList());
        when(taskRepository.update(any(Task.class))).thenReturn(Collections.emptyList());

        TaskStep step = createStep(1L, TASK_ID, 1, TaskStep.WAITING);
        when(stepRepository.findStepsByTaskId(TASK_ID)).thenReturn(Collections.singletonList(step));

        taskTrigger.checkPendingTasksToStart();

        verify(taskRepository).update(any(Task.class));
        verify(eventPublisher).publishEvent(any(ExecuteStepEvent.class));
    }

    @Test
    void testCheckPendingTasksToStart_GroupTask_GroupExecuting() {
        // 准备：组任务 + 组内已有任务在执行
        Task task = createTask(TASK_ID, Task.PENDING, 1, null, GROUP_ID, null);

        Task executingGroupTask = createTask(2L, Task.EXECUTING, 1, null, GROUP_ID, null);

        when(taskRepository.getTasks(Task.PENDING, null, null, null, null, null, null, null, null))
                .thenReturn(Collections.singletonList(task));
        when(taskRepository.getTasks(Task.EXECUTING, 1, null, null, GROUP_ID, null, null, null, null))
                .thenReturn(Collections.singletonList(executingGroupTask));

        taskTrigger.checkPendingTasksToStart();

        verify(taskRepository, never()).update(any(Task.class));
    }

    @Test
    void testCheckPendingTasksToStart_NoTemplate() {
        // 准备：无模板的任务，直接完成
        Task task = createTask(TASK_ID, Task.PENDING, 0, ROBOT_ID, null, null);
        task.setTemplateId(null);

        when(taskRepository.getTasks(Task.PENDING, null, null, null, null, null, null, null, null))
                .thenReturn(Collections.singletonList(task));
        when(taskRepository.getTasks(Task.EXECUTING, 0, null, ROBOT_ID, null, null, null, null, null))
                .thenReturn(Collections.emptyList());
        when(stepRepository.getSteps(TaskStep.EXECUTING, null, ROBOT_ID))
                .thenReturn(Collections.emptyList());
        when(taskRepository.update(any(Task.class))).thenReturn(Collections.emptyList());

        taskTrigger.checkPendingTasksToStart();

        verify(taskService).completeTask(any(Task.class));
        verify(eventPublisher, never()).publishEvent(any(ExecuteStepEvent.class));
    }

    // ==================== 3. 步骤完成事件监听测试 ====================

    @Test
    void testOnStepCompleted_Success_WithNextStep() {
        Task task = createTask(TASK_ID, Task.EXECUTING, 0, ROBOT_ID, null, null);
        task.setTemplateId(1L);

        TaskStep completedStep = createStep(1L, TASK_ID, 1, TaskStep.FINISHED);
        TaskStep nextStep = createStep(2L, TASK_ID, 2, TaskStep.WAITING);

        when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(task));
        when(stepRepository.findStepsByTaskId(TASK_ID))
                .thenReturn(Arrays.asList(completedStep, nextStep));

        StepCompletedEvent event = new StepCompletedEvent(this, TASK_ID, 1L, true);
        taskTrigger.onStepCompleted(event);

        verify(eventPublisher).publishEvent(any(ExecuteStepEvent.class));
        verify(taskService, never()).completeTask(any());
    }

    @Test
    void testOnStepCompleted_Success_AllStepsDone() {
        Task task = createTask(TASK_ID, Task.EXECUTING, 0, ROBOT_ID, null, null);

        TaskStep completedStep = createStep(1L, TASK_ID, 1, TaskStep.FINISHED);

        when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(task));
        when(stepRepository.findStepsByTaskId(TASK_ID))
                .thenReturn(Collections.singletonList(completedStep));

        StepCompletedEvent event = new StepCompletedEvent(this, TASK_ID, 1L, true);
        taskTrigger.onStepCompleted(event);

        verify(taskService).completeTask(task);
        verify(eventPublisher, never()).publishEvent(any(ExecuteStepEvent.class));
    }

    @Test
    void testOnStepCompleted_Failure() {
        Task task = createTask(TASK_ID, Task.EXECUTING, 0, ROBOT_ID, null, null);

        when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(task));
        when(taskRepository.update(any(Task.class))).thenReturn(Collections.emptyList());

        StepCompletedEvent event = new StepCompletedEvent(this, TASK_ID, 1L, false);
        taskTrigger.onStepCompleted(event);

        verify(taskRepository).update(argThat(t ->
                t.getStatus() == Task.PAUSED && t.getRiskLevel() == 2
        ));
        verify(eventPublisher, never()).publishEvent(any(ExecuteStepEvent.class));
    }

    @Test
    void testOnStepCompleted_TaskNotExecuting() {
        Task task = createTask(TASK_ID, Task.PAUSED, 0, ROBOT_ID, null, null);

        when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(task));

        StepCompletedEvent event = new StepCompletedEvent(this, TASK_ID, 1L, true);
        taskTrigger.onStepCompleted(event);

        verify(stepRepository, never()).findStepsByTaskId(any());
    }

    // ==================== 4. 机器人预警事件处理测试 ====================

    @Test
    void testHandleRobotWarning_SingleTask_WithWarning() {
        Task task = createTask(TASK_ID, Task.EXECUTING, 0, ROBOT_ID, null, null);

        when(taskRepository.getTasks(null, 0, null, ROBOT_ID, null, null, null, null, null))
                .thenReturn(Collections.singletonList(task));
        when(robotService.selectRobotsById(ROBOT_ID)).thenReturn(new Robot());
        when(robotWarningsService.countUnresolvedByRobotId(ROBOT_ID)).thenReturn(1);
        when(taskRepository.update(any(Task.class))).thenReturn(Collections.emptyList());

        RobotWarningEvent event = new RobotWarningEvent(this, ROBOT_ID, "0", "1",RobotsConstants.UNRESOLVED,
                true);
        taskTrigger.handleRobotWarning(event);

        verify(taskRepository).update(argThat(t -> t.getRiskLevel() == 2));
        verify(taskLogService).record(eq(TASK_ID), isNull(), eq(TaskLogEventType.ROBOT_STATUS_CHANGE),
                contains("高风险"), eq("system"), isNull());
    }

    @Test
    void testHandleRobotWarning_SingleTask_WarningResolved() {
        Task task = createTask(TASK_ID, Task.PENDING, 0, ROBOT_ID, null, null);

        when(taskRepository.getTasks(null, 0, null, ROBOT_ID, null, null, null, null, null))
                .thenReturn(Collections.singletonList(task));
        when(robotService.selectRobotsById(ROBOT_ID)).thenReturn(new Robot());
        when(robotWarningsService.countUnresolvedByRobotId(ROBOT_ID)).thenReturn(0);
        when(taskRepository.update(any(Task.class))).thenReturn(Collections.emptyList());

        RobotWarningEvent event = new RobotWarningEvent(this, ROBOT_ID, "0", "1",RobotsConstants.RESOLVED,
                false);
        taskTrigger.handleRobotWarning(event);

        verify(taskRepository).update(argThat(t -> t.getRiskLevel() == 0));
        verify(taskLogService).record(eq(TASK_ID), isNull(), eq(TaskLogEventType.ROBOT_STATUS_CHANGE),
                contains("风险清除"), eq("system"), isNull());
    }

    @Test
    void testHandleRobotWarning_GroupTask_WithAssignedRobotWarning() {
        Task task = createTask(TASK_ID, Task.EXECUTING, 1, null, GROUP_ID, null);

        Robot robot = new Robot();
        robot.setId(ROBOT_ID);
        robot.setGroupId(GROUP_ID);

        TaskStep step = createStep(1L, TASK_ID, 1, TaskStep.EXECUTING);
        step.setAssignedRobotId(ROBOT_ID);

        when(taskRepository.getTasks(null, 0, null, ROBOT_ID, null, null, null, null, null))
                .thenReturn(Collections.emptyList());
        when(robotService.selectRobotsById(ROBOT_ID)).thenReturn(robot);
        when(taskRepository.getTasks(null, 1, null, null, GROUP_ID, null, null, null, null))
                .thenReturn(Collections.singletonList(task));
        when(stepRepository.countByAssignedRobotIdAndTaskIdAndStatusIn(
                eq(ROBOT_ID), eq(TASK_ID), anyList())).thenReturn(1L);
        when(robotWarningsService.countUnresolvedByRobotId(ROBOT_ID)).thenReturn(1);
        when(taskRepository.update(any(Task.class))).thenReturn(Collections.emptyList());

        RobotWarningEvent event = new RobotWarningEvent(this, ROBOT_ID, "0", "1",RobotsConstants.RESOLVED,
                false);
        taskTrigger.handleRobotWarning(event);

        verify(taskRepository).update(argThat(t -> t.getRiskLevel() == 2));
    }

    @Test
    void testHandleRobotWarning_GroupTask_RobotNotAssigned() {
        Task task = createTask(TASK_ID, Task.EXECUTING, 1, null, GROUP_ID, null);

        Robot robot = new Robot();
        robot.setId(ROBOT_ID);
        robot.setGroupId(GROUP_ID);

        when(taskRepository.getTasks(null, 0, null, ROBOT_ID, null, null, null, null, null))
                .thenReturn(Collections.emptyList());
        when(robotService.selectRobotsById(ROBOT_ID)).thenReturn(robot);
        when(taskRepository.getTasks(null, 1, null, null, GROUP_ID, null, null, null, null))
                .thenReturn(Collections.singletonList(task));
        when(stepRepository.countByAssignedRobotIdAndTaskIdAndStatusIn(
                eq(ROBOT_ID), eq(TASK_ID), anyList())).thenReturn(0L);
        when(taskRepository.update(any(Task.class))).thenReturn(Collections.emptyList());

        RobotWarningEvent event = new RobotWarningEvent(this, ROBOT_ID, "0", "1",RobotsConstants.UNRESOLVED,
                false);
        taskTrigger.handleRobotWarning(event);

        // 机器人未分配到任务步骤，风险等级应该为0
        verify(taskRepository).update(argThat(t -> t.getRiskLevel() == 0));
    }

    @Test
    void testHandleRobotWarning_TaskFinished_ShouldIgnore() {
        Task task = createTask(TASK_ID, Task.FINISHED, 0, ROBOT_ID, null, null);

        when(taskRepository.getTasks(null, 0, null, ROBOT_ID, null, null, null, null, null))
                .thenReturn(Collections.singletonList(task));
        when(robotService.selectRobotsById(ROBOT_ID)).thenReturn(new Robot());

        RobotWarningEvent event = new RobotWarningEvent(this, ROBOT_ID, "0","1",RobotsConstants.UNRESOLVED,false);
        taskTrigger.handleRobotWarning(event);

        // 已完成的任务不应该被更新
        verify(taskRepository, never()).update(any(Task.class));
        verify(taskLogService, never()).record(any(), any(), any(), any(), any(), any());
    }

    // ==================== 辅助方法 ====================

    private Task createTask(Long id, Byte status, int isGroupTask, Long robotId,
                            Long groupId, Integer triggerType) {
        Task task = new Task();
        task.setId(id);
        task.setStatus(status);
        task.setIsGroupTask(isGroupTask);
        task.setRobotId(robotId);
        task.setRobotGroupId(groupId);
        task.setTaskType(triggerType);
        task.setPendingOrder(0);
        task.setGlobalPendingOrder(0);
        task.setRiskLevel(0);
        return task;
    }

    private TaskStep createStep(Long id, Long taskId, int orderNum, Byte status) {
        TaskStep step = new TaskStep();
        step.setId(id);
        step.setTaskId(taskId);
        step.setOrderNum(orderNum);
        step.setStatus(status);
        return step;
    }
}