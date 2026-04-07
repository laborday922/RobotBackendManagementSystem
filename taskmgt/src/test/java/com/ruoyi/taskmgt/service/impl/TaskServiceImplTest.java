package com.ruoyi.taskmgt.service.impl;

import com.ruoyi.app.domain.TAppLibrary;
import com.ruoyi.app.service.ITAppConstraintService;
import com.ruoyi.app.service.ITAppLibraryService;
import com.ruoyi.app.service.ITAppParamService;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.exception.task.TaskmgtException;
import com.ruoyi.common.threadlocal.TenantContext;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.robots.domain.Robot;
import com.ruoyi.robots.service.IRobotGroupsService;
import com.ruoyi.robots.service.IRobotWarningsService;
import com.ruoyi.robots.service.IRobotsService;
import com.ruoyi.taskmgt.constants.TaskLogEventType;
import com.ruoyi.taskmgt.domain.StepRepository;
import com.ruoyi.taskmgt.domain.TaskRepository;
import com.ruoyi.taskmgt.domain.TemplateRepository;
import com.ruoyi.taskmgt.domain.bo.*;
import com.ruoyi.taskmgt.service.IStepService;
import com.ruoyi.taskmgt.service.vo.TaskVo;
import com.ruoyi.taskmgt.utils.ExpressionEvaluator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.support.MessageSourceAccessor;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    @Mock private TaskRepository taskRepository;
    @Mock private MessageSourceAccessor messageSourceAccessor;
    @Mock private RedisCache redisUtil;
    @Mock private TemplateRepository templateRepository;
    @Mock private StepReuseService stepReuseService;
    @Mock private IStepService stepService;
    @Mock private StepRepository stepRepository;
    @Mock private TaskLogReuseService taskLogService;
    @Mock private IRobotsService robotService;
    @Mock private IRobotWarningsService robotWarningsService;
    @Mock private IRobotGroupsService robotGroupsService;
    @Mock private ITAppLibraryService appLibraryService;
    @Mock private ITAppParamService appParamService;
    @Mock private ITAppConstraintService constraintService;
    @Mock private ExpressionEvaluator expressionEvaluator;

    @InjectMocks
    private TaskServiceImpl taskService;

    private static final Long TENANT_ID = 1L;
    private static final Long TASK_ID = 100L;
    private static final Long TEMPLATE_ID = 200L;
    private static final Long ROBOT_ID = 300L;
    private static final Long ROBOT_GROUP_ID = 400L;
    private static final String TEST_USER = "testUser";

    /**
     * 创建一个默认的任务对象（未持久化，仅填充必要字段）
     */
    private Task createDefaultTask() {
        return Task.builder()
                .id(TASK_ID)
                .name("测试任务")
                .templateId(TEMPLATE_ID)
                .taskType(1)
                .priority(5)
                .status(Task.NOTSTART)
                .isGroupTask(0)
                .robotId(ROBOT_ID)
                .tenantId(TENANT_ID)
                .build();
    }

    /**
     * 创建一个默认的模板对象
     */
    private Template createDefaultTemplate() {
        Template template = new Template();
        template.setId(TEMPLATE_ID);
        template.setName("通用测试模板");
        template.setAppId(10L);
        template.setStatus(Template.ENABLED);
        template.setFormContent("{\"fields\":[]}");
        template.setWorkflow("{\"steps\":[]}");
        return template;
    }

    /**
     * 创建一个默认的应用对象
     */
    private TAppLibrary createDefaultApp() {
        TAppLibrary app = new TAppLibrary();
        app.setId(10L);
        app.setAppName("测试应用");
        return app;
    }

    /**
     * 创建一个默认的机器人对象
     */
    private Robot createDefaultRobot() {
        Robot robot = new Robot();
        robot.setId(ROBOT_ID);
        robot.setName("测试机器人");
        robot.setGroupId(ROBOT_GROUP_ID);
        robot.setStatus(1);      // 在线
        robot.setHardwareStatus(0); // 正常
        robot.setTaskStatus(2);  // 闲置
        robot.setBattery(100);
        return robot;
    }

    @BeforeEach
    void setUp() {
        lenient().when(messageSourceAccessor.getMessage(anyString())).thenReturn("测试消息");
    }
    @Test
    void testCreateTask_Success() {
        try (var tenantMock = mockStatic(TenantContext.class);
             var securityMock = mockStatic(SecurityUtils.class)) {

            tenantMock.when(TenantContext::get).thenReturn(TENANT_ID);
            securityMock.when(SecurityUtils::getUsername).thenReturn(TEST_USER);
            securityMock.when(() -> SecurityUtils.isAdmin(anyLong())).thenReturn(false);

            Task task = createDefaultTask();
            Template template = createDefaultTemplate();
            TAppLibrary app = createDefaultApp();

            when(templateRepository.findById(TEMPLATE_ID)).thenReturn(Optional.of(template));
            when(appLibraryService.selectTAppLibraryById(10L)).thenReturn(app);
            when(appParamService.selectTAppParamList(any())).thenReturn(new ArrayList<>());
            when(constraintService.selectTAppConstraintList(any())).thenReturn(new ArrayList<>());
            when(stepReuseService.getStandardSteps(any(), anyMap(), anyMap())).thenReturn(new ArrayList<>());

            Task insertedTask = createDefaultTask();
            insertedTask.setId(TASK_ID);
            when(taskRepository.insert(any(Task.class))).thenAnswer(inv -> {
                Task arg = inv.getArgument(0);
                arg.setId(TASK_ID);
                return arg;
            });
            when(templateRepository.getTemplateNameById(TEMPLATE_ID)).thenReturn("通用测试模板");

            TaskVo result = taskService.createTask(task);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(TASK_ID);
            assertThat(result.getStatus()).isEqualTo(Task.NOTSTART);
            verify(stepService).createSteps(eq(TASK_ID), anyList());
            verify(taskLogService).record(eq(TASK_ID), isNull(), eq(TaskLogEventType.TASK_CREATE),
                    anyString(), eq(TEST_USER), eq(TENANT_ID));
        }
    }

    @Test
    void testCreateTask_TemplateNotFound_ThrowsException() {
        try (var tenantMock = mockStatic(TenantContext.class);
             var securityMock = mockStatic(SecurityUtils.class)) {
            tenantMock.when(TenantContext::get).thenReturn(TENANT_ID);
            securityMock.when(() -> SecurityUtils.isAdmin(anyLong())).thenReturn(false);
            securityMock.when(SecurityUtils::getUsername).thenReturn(TEST_USER);
            Task task = createDefaultTask();
            when(taskRepository.insert(any(Task.class))).thenAnswer(inv -> {
                Task arg = inv.getArgument(0);
                arg.setId(TASK_ID);
                return arg;
            });
            when(templateRepository.findById(TEMPLATE_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> taskService.createTask(task))
                    .isInstanceOf(TaskmgtException.class);
        }
    }

    @Test
    void testUpdateTask_Success() {
        try (var securityMock = mockStatic(SecurityUtils.class)) {
            securityMock.when(SecurityUtils::getUsername).thenReturn(TEST_USER);

            Task existingTask = createDefaultTask();
            existingTask.setStatus(Task.NOTSTART);
            Task updateTask = createDefaultTask();
            updateTask.setName("更新后的名称");
            updateTask.setRobotId(ROBOT_ID);

            when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(existingTask));
            when(robotWarningsService.countUnresolvedByRobotId(ROBOT_ID)).thenReturn(0);
            when(taskRepository.update(any(Task.class))).thenReturn(new ArrayList<>()); // 使用可变列表

            taskService.updateTask(updateTask);

            ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
            verify(taskRepository).update(captor.capture());
            assertThat(captor.getValue().getName()).isEqualTo("更新后的名称");
            verify(redisUtil).deleteObject(anyList());
        }
    }

    @Test
    void testUpdateTask_WhenExecuting_ThrowsException() {
        try (var securityMock = mockStatic(SecurityUtils.class);
             var tenantMock = mockStatic(TenantContext.class)) {
            tenantMock.when(TenantContext::get).thenReturn(TENANT_ID);
            securityMock.when(() -> SecurityUtils.isAdmin(anyLong())).thenReturn(false);
            securityMock.when(SecurityUtils::getUsername).thenReturn(TEST_USER);
            Task updateTask = createDefaultTask();
            updateTask.setStatus(Task.EXECUTING);
            assertThatThrownBy(() -> taskService.updateTask(updateTask))
                    .isInstanceOf(TaskmgtException.class);
        }
    }

    @Test
    void testDeleteTask_Success() {
        try (var tenantMock = mockStatic(TenantContext.class);
             var securityMock = mockStatic(SecurityUtils.class)) {
            tenantMock.when(TenantContext::get).thenReturn(TENANT_ID);
            securityMock.when(SecurityUtils::getUsername).thenReturn(TEST_USER);
            securityMock.when(() -> SecurityUtils.isAdmin(anyLong())).thenReturn(false);

            Task task = createDefaultTask();
            task.setStatus(Task.DISABLED);
            when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(task));
            when(taskRepository.update(any(Task.class))).thenReturn(new ArrayList<>());
            when(stepRepository.deleteStepsByTaskId(TASK_ID)).thenReturn(new HashSet<>());

            taskService.deleteTask(TASK_ID);

            ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
            verify(taskRepository).update(captor.capture());
            assertThat(captor.getValue().getStatus()).isEqualTo(Task.DELETED);
            verify(redisUtil).deleteObject(anyList());
            verify(taskLogService).record(eq(TASK_ID), isNull(), eq(TaskLogEventType.TASK_DELETE),
                    anyString(), eq(TEST_USER), eq(TENANT_ID));
        }
    }

    @Test
    void testDeleteTask_NotDisabled_ThrowsException() {
        Task task = createDefaultTask();
        task.setStatus(Task.NOTSTART);
        when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(task));

        assertThatThrownBy(() -> taskService.deleteTask(TASK_ID))
                .isInstanceOf(TaskmgtException.class);
    }

    @Test
    void testBanTask_Success() {
        Task task = createDefaultTask();
        task.setStatus(Task.NOTSTART);
        when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(task));
        when(taskRepository.update(any(Task.class))).thenReturn(new ArrayList<>());

        taskService.banTask(TASK_ID);

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).update(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(Task.DISABLED);
        verify(redisUtil).deleteObject(anyList());
    }

    @Test
    void testResumeTask_Success() {
        Task task = createDefaultTask();
        task.setStatus(Task.DISABLED);
        when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(task));
        when(taskRepository.update(any(Task.class))).thenReturn(new ArrayList<>());

        taskService.resumeTask(TASK_ID);

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).update(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(Task.NOTSTART);
    }

    @Test
    void testPauseTask_Success() {
        try (var tenantMock = mockStatic(TenantContext.class);
             var securityMock = mockStatic(SecurityUtils.class)) {
            tenantMock.when(TenantContext::get).thenReturn(TENANT_ID);
            securityMock.when(SecurityUtils::getUsername).thenReturn(TEST_USER);
            securityMock.when(() -> SecurityUtils.isAdmin(anyLong())).thenReturn(false);

            Task task = createDefaultTask();
            task.setStatus(Task.EXECUTING);
            when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(task));
            when(taskRepository.update(any(Task.class))).thenReturn(new ArrayList<>());
            when(stepReuseService.pauseStepsByTaskId(TASK_ID)).thenReturn(new ArrayList<>());

            taskService.pauseTask(TASK_ID);

            verify(stepReuseService).pauseStepsByTaskId(TASK_ID);
            verify(taskLogService).record(eq(TASK_ID), isNull(), eq(TaskLogEventType.TASK_PAUSE),
                    anyString(), eq(TEST_USER), eq(TENANT_ID));
        }
    }

    @Test
    void testContinueTask_WhenRobotAvailable() {
        try (var tenantMock = mockStatic(TenantContext.class);
             var securityMock = mockStatic(SecurityUtils.class)) {
            tenantMock.when(TenantContext::get).thenReturn(TENANT_ID);
            securityMock.when(SecurityUtils::getUsername).thenReturn(TEST_USER);
            securityMock.when(() -> SecurityUtils.isAdmin(anyLong())).thenReturn(false);

            // 创建一个已暂停的任务
            Task task = createDefaultTask();
            task.setStatus(Task.PAUSED);
            task.setIsGroupTask(0);
            task.setRobotId(ROBOT_ID);
            when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(task));

            // Mock 机器人可用性检查所需的所有依赖
            Robot robot = createDefaultRobot();
            when(robotService.selectRobotsById(ROBOT_ID)).thenReturn(robot);
            when(robotWarningsService.countUnresolvedByRobotId(ROBOT_ID)).thenReturn(0);
            when(stepRepository.countByAssignedRobotIdAndTaskIdAndStatusIn(eq(ROBOT_ID), isNull(), anyList())).thenReturn(0L);
            when(taskRepository.findByRobotIdAndStatus(ROBOT_ID, Task.EXECUTING)).thenReturn(Collections.emptyList());

            when(taskRepository.update(any(Task.class))).thenReturn(new ArrayList<>());
            when(stepReuseService.continueStepsByTaskId(TASK_ID)).thenReturn(new ArrayList<>());

            taskService.continueTask(TASK_ID);

            ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
            verify(taskRepository).update(captor.capture());
            assertThat(captor.getValue().getStatus()).isEqualTo(Task.EXECUTING);
            verify(stepReuseService).continueStepsByTaskId(TASK_ID);
        }
    }

    @Test
    void testTerminateTask_Success() {
        try (var tenantMock = mockStatic(TenantContext.class);
             var securityMock = mockStatic(SecurityUtils.class)) {
            tenantMock.when(TenantContext::get).thenReturn(TENANT_ID);
            securityMock.when(SecurityUtils::getUsername).thenReturn(TEST_USER);
            securityMock.when(() -> SecurityUtils.isAdmin(anyLong())).thenReturn(false);

            Task task = createDefaultTask();
            task.setStatus(Task.EXECUTING);
            when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(task));
            when(taskRepository.update(any(Task.class))).thenReturn(new ArrayList<>());
            when(stepReuseService.terminatedStepsByTaskId(TASK_ID)).thenReturn(new ArrayList<>());

            taskService.terminateTask(TASK_ID, "手动终止");

            ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
            verify(taskRepository).update(captor.capture());
            assertThat(captor.getValue().getStatus()).isEqualTo(Task.TERMINATED);
            assertThat(captor.getValue().getTerminateReason()).isEqualTo("手动终止");
        }
    }

    @Test
    void testCancelTask_Success() {
        try (var tenantMock = mockStatic(TenantContext.class);
             var securityMock = mockStatic(SecurityUtils.class)) {
            tenantMock.when(TenantContext::get).thenReturn(TENANT_ID);
            securityMock.when(SecurityUtils::getUsername).thenReturn(TEST_USER);
            securityMock.when(() -> SecurityUtils.isAdmin(anyLong())).thenReturn(false);

            Task task = createDefaultTask();
            task.setStatus(Task.PENDING);
            when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(task));
            when(taskRepository.update(any(Task.class))).thenReturn(new ArrayList<>());
            when(stepReuseService.cancelStepsByTaskId(TASK_ID)).thenReturn(new ArrayList<>());

            taskService.cancelTask(TASK_ID);

            ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
            verify(taskRepository).update(captor.capture());
            assertThat(captor.getValue().getStatus()).isEqualTo(Task.NOTSTART);
        }
    }

    @Test
    void testResolveRisk_Success() {
        try (var tenantMock = mockStatic(TenantContext.class);
             var securityMock = mockStatic(SecurityUtils.class)) {
            tenantMock.when(TenantContext::get).thenReturn(TENANT_ID);
            securityMock.when(SecurityUtils::getUsername).thenReturn(TEST_USER);
            securityMock.when(() -> SecurityUtils.isAdmin(anyLong())).thenReturn(false);

            Task task = createDefaultTask();
            task.setStatus(Task.EXECUTING);
            task.setIsGroupTask(0);
            task.setRobotId(ROBOT_ID);
            task.setRiskLevel(2);
            when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(task));
            when(robotWarningsService.countUnresolvedByRobotId(ROBOT_ID)).thenReturn(0);
            when(taskRepository.update(any(Task.class))).thenReturn(new ArrayList<>());

            boolean result = taskService.resolveRisk(TASK_ID);

            assertThat(result).isTrue();
            ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
            verify(taskRepository).update(captor.capture());
            assertThat(captor.getValue().getRiskLevel()).isEqualTo(0);
            verify(taskLogService).record(eq(TASK_ID), isNull(), eq("RISK_RESOLVED"),
                    anyString(), eq(TEST_USER), eq(TENANT_ID));
        }
    }

    @Test
    void testRetrieveTasks_WithFilters() {
        try (var tenantMock = mockStatic(TenantContext.class);
             var securityMock = mockStatic(SecurityUtils.class)) {
            tenantMock.when(TenantContext::get).thenReturn(TENANT_ID);
            securityMock.when(() -> SecurityUtils.isAdmin(anyLong())).thenReturn(false);

            Task task1 = createDefaultTask();
            task1.setId(1L);
            task1.setName("任务1");
            Task task2 = createDefaultTask();
            task2.setId(2L);
            task2.setName("任务2");

            when(taskRepository.getTasks(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                    .thenReturn(List.of(task1, task2));
            when(templateRepository.getTemplateNameById(TEMPLATE_ID)).thenReturn("模板A");
            when(stepRepository.findStepsByTaskId(anyLong())).thenReturn(new ArrayList<>());

            List<TaskVo> result = taskService.retrieveTasks(Task.NOTSTART, null, "任务",
                    null, null, null, null, null);

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getTemplateName()).isEqualTo("模板A");
        }
    }

    @Test
    void testGetTask_Success() {
        Task task = createDefaultTask();
        Robot robot = createDefaultRobot();
        when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(task));
        when(templateRepository.getTemplateNameById(TEMPLATE_ID)).thenReturn("模板名称");
        when(robotService.selectRobotsById(ROBOT_ID)).thenReturn(robot);

        TaskVo vo = taskService.getTask(TASK_ID);

        assertThat(vo).isNotNull();
        assertThat(vo.getId()).isEqualTo(TASK_ID);
        assertThat(vo.getRobotName()).isEqualTo("测试机器人");
        assertThat(vo.getTemplateName()).isEqualTo("模板名称");
    }

    @Test
    void testUpdateGlobalOrder_Success() {
        try (var tenantMock = mockStatic(TenantContext.class);
             var securityMock = mockStatic(SecurityUtils.class)) {
            tenantMock.when(TenantContext::get).thenReturn(TENANT_ID);
            securityMock.when(() -> SecurityUtils.isAdmin(anyLong())).thenReturn(false);
            securityMock.when(SecurityUtils::getUsername).thenReturn(TEST_USER);

            Task taskA = createDefaultTask();
            taskA.setId(1L);
            taskA.setStatus(Task.PENDING);
            taskA.setIsGroupTask(0);
            taskA.setRobotId(10L);
            taskA.setPendingOrder(0);
            taskA.setGlobalPendingOrder(0);

            Task taskB = createDefaultTask();
            taskB.setId(2L);
            taskB.setStatus(Task.PENDING);
            taskB.setIsGroupTask(0);
            taskB.setRobotId(10L);
            taskB.setPendingOrder(1);
            taskB.setGlobalPendingOrder(1);

            when(taskRepository.getTasks(eq(Task.PENDING), isNull(), isNull(), isNull(), isNull(),
                    isNull(), isNull(), isNull(), any())).thenReturn(List.of(taskA, taskB));
            when(taskRepository.update(any(Task.class))).thenReturn(new ArrayList<>());

            taskService.updateGlobalOrder(List.of(1L, 2L));

            verify(taskRepository, times(2)).update(any(Task.class));
        }
    }

    @Test
    void testUpdateLocalOrder_Success() {
        try (var tenantMock = mockStatic(TenantContext.class);
             var securityMock = mockStatic(SecurityUtils.class)) {
            tenantMock.when(TenantContext::get).thenReturn(TENANT_ID);
            securityMock.when(() -> SecurityUtils.isAdmin(anyLong())).thenReturn(false);
            securityMock.when(SecurityUtils::getUsername).thenReturn(TEST_USER);

            Task taskA = createDefaultTask();
            taskA.setId(1L);
            taskA.setStatus(Task.PENDING);
            taskA.setRobotId(10L);
            taskA.setIsGroupTask(0);
            taskA.setPendingOrder(0);
            taskA.setGlobalPendingOrder(0);

            Task taskB = createDefaultTask();
            taskB.setId(2L);
            taskB.setStatus(Task.PENDING);
            taskB.setRobotId(10L);
            taskB.setIsGroupTask(0);
            taskB.setPendingOrder(1);
            taskB.setGlobalPendingOrder(1);

            when(taskRepository.getTasks(eq(Task.PENDING), eq(0), isNull(), eq(10L), isNull(),
                    isNull(), isNull(), isNull(), any())).thenReturn(List.of(taskA, taskB));
            when(taskRepository.getTasks(eq(Task.PENDING), isNull(), isNull(), isNull(), isNull(),
                    isNull(), isNull(), isNull(), any())).thenReturn(List.of(taskA, taskB));
            when(taskRepository.update(any(Task.class))).thenReturn(new ArrayList<>());

            taskService.updateLocalOrder(10L, false, List.of(2L, 1L));

            ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
            verify(taskRepository, atLeast(2)).update(captor.capture());
            boolean hasPendingOrderUpdate = captor.getAllValues().stream().anyMatch(t -> t.getPendingOrder() != null);
            assertThat(hasPendingOrderUpdate).isTrue();
        }
    }

    @Test
    void testGetAbnormalTasks() {
        try (var tenantMock = mockStatic(TenantContext.class);
             var securityMock = mockStatic(SecurityUtils.class)) {
            tenantMock.when(TenantContext::get).thenReturn(TENANT_ID);
            securityMock.when(() -> SecurityUtils.isAdmin(anyLong())).thenReturn(false);

            Task task = createDefaultTask();
            task.setRiskLevel(2);
            task.setIsGroupTask(0);
            task.setRobotId(ROBOT_ID);

            when(taskRepository.getTasks(isNull(), isNull(), isNull(), eq(ROBOT_ID), isNull(),
                    isNull(), eq(2), isNull(), any())).thenReturn(List.of(task));

            Robot robot = createDefaultRobot();
            when(robotService.selectRobotsById(ROBOT_ID)).thenReturn(robot);
            when(robotWarningsService.selectRobotWarningsList(any())).thenReturn(new ArrayList<>());

            var result = taskService.getAbnormalTasks(2, ROBOT_ID, null);
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getRobotName()).isEqualTo("测试机器人");
        }
    }
}