package com.ruoyi.taskmgt.service.impl;

import com.ruoyi.app.domain.TAppConstraint;
import com.ruoyi.app.domain.TAppLibrary;
import com.ruoyi.app.domain.TAppParam;
import com.ruoyi.app.service.ITAppConstraintService;
import com.ruoyi.app.service.ITAppLibraryService;
import com.ruoyi.app.service.ITAppParamService;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.exception.task.TaskmgtException;
import com.ruoyi.common.threadlocal.TenantContext;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.robots.domain.Robot;
import com.ruoyi.robots.domain.RobotWarnings;
import com.ruoyi.robots.service.IRobotGroupsService;
import com.ruoyi.robots.service.IRobotWarningsService;
import com.ruoyi.robots.service.IRobotsService;
import com.ruoyi.taskmgt.constants.TaskLogEventType;
import com.ruoyi.taskmgt.domain.StepRepository;
import com.ruoyi.taskmgt.domain.TaskRepository;
import com.ruoyi.taskmgt.domain.TemplateRepository;
import com.ruoyi.taskmgt.domain.bo.*;
import com.ruoyi.taskmgt.service.IStepService;
import com.ruoyi.taskmgt.service.vo.TaskAbnormalVo;
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
public class TaskServiceImplTest {

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

    @BeforeEach
    void setUp() {
        lenient().when(messageSourceAccessor.getMessage(anyString())).thenReturn("测试消息");
        lenient().when(messageSourceAccessor.getMessage(anyString(), (Object[]) any())).thenReturn("测试消息");
    }

    // ==================== 辅助方法 ====================

    private Task createDefaultTask() {
        Task task = new Task();
        task.setId(TASK_ID);
        task.setName("测试任务");
        task.setTemplateId(TEMPLATE_ID);
        task.setTaskType(1);
        task.setPriority(5);
        task.setStatus(Task.NOTSTART);
        task.setIsGroupTask(0);
        task.setRobotId(ROBOT_ID);
        task.setTenantId(TENANT_ID);
        return task;
    }

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

    private TAppLibrary createDefaultApp() {
        TAppLibrary app = new TAppLibrary();
        app.setId(10L);
        app.setAppName("测试应用");
        return app;
    }

    private Robot createDefaultRobot() {
        Robot robot = new Robot();
        robot.setId(ROBOT_ID);
        robot.setName("测试机器人");
        robot.setGroupId(ROBOT_GROUP_ID);
        robot.setStatus(1);
        robot.setHardwareStatus(0);
        robot.setTaskStatus(2);
        robot.setBattery(100);
        return robot;
    }

    // ==================== 1. createTask 相关测试 ====================

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
    void testCreateTask_AdminUser() {
        try (var tenantMock = mockStatic(TenantContext.class);
             var securityMock = mockStatic(SecurityUtils.class)) {

            tenantMock.when(TenantContext::get).thenReturn(TENANT_ID);
            securityMock.when(SecurityUtils::getUsername).thenReturn(TEST_USER);
            securityMock.when(() -> SecurityUtils.isAdmin(anyLong())).thenReturn(true);

            Task task = createDefaultTask();
            task.setTenantId(null);
            Template template = createDefaultTemplate();
            TAppLibrary app = createDefaultApp();

            when(templateRepository.findById(TEMPLATE_ID)).thenReturn(Optional.of(template));
            when(appLibraryService.selectTAppLibraryById(10L)).thenReturn(app);
            when(appParamService.selectTAppParamList(any())).thenReturn(new ArrayList<>());
            when(constraintService.selectTAppConstraintList(any())).thenReturn(new ArrayList<>());
            when(stepReuseService.getStandardSteps(any(), anyMap(), anyMap())).thenReturn(new ArrayList<>());

            when(taskRepository.insert(any(Task.class))).thenAnswer(inv -> {
                Task arg = inv.getArgument(0);
                arg.setId(TASK_ID);
                return arg;
            });
            when(templateRepository.getTemplateNameById(TEMPLATE_ID)).thenReturn("通用测试模板");

            TaskVo result = taskService.createTask(task);

            assertThat(result).isNotNull();
            // 管理员时 tenantId 应为 null（因为 isAdmin 返回 true）
            verify(taskRepository).insert(argThat(t -> t.getTenantId() == null));
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
    void testCreateTask_ConstraintViolation_ThrowsException() {
        try (var tenantMock = mockStatic(TenantContext.class);
             var securityMock = mockStatic(SecurityUtils.class)) {

            tenantMock.when(TenantContext::get).thenReturn(TENANT_ID);
            securityMock.when(SecurityUtils::getUsername).thenReturn(TEST_USER);
            securityMock.when(() -> SecurityUtils.isAdmin(anyLong())).thenReturn(false);

            Task task = createDefaultTask();
            task.setFormContent("{\"field1\":\"value1\"}");
            Template template = createDefaultTemplate();
            template.setFormContent("{\"fields\":[{\"id\":\"field1\",\"type\":\"string\"}]}");
            TAppLibrary app = createDefaultApp();

            // 确保 insert 返回的 task 有 templateId
            Task insertedTask = createDefaultTask();
            insertedTask.setTemplateId(TEMPLATE_ID);  // 必须有 templateId
            insertedTask.setFormContent("{\"field1\":\"value1\"}");

            TAppConstraint constraint = new TAppConstraint();
            constraint.setExpression("form_data.field1 == 'wrong'");
            constraint.setErrorMessage("约束验证失败");

            when(templateRepository.findById(TEMPLATE_ID)).thenReturn(Optional.of(template));
            when(appLibraryService.selectTAppLibraryById(10L)).thenReturn(app);
            when(appParamService.selectTAppParamList(any())).thenReturn(new ArrayList<>());
            when(constraintService.selectTAppConstraintList(any())).thenReturn(List.of(constraint));
            when(expressionEvaluator.evaluateBoolean(anyString(), anyMap())).thenReturn(false);
            when(taskRepository.insert(any(Task.class))).thenReturn(insertedTask);

            assertThatThrownBy(() -> taskService.createTask(task))
                    .isInstanceOf(TaskmgtException.class);
        }
    }

    // ==================== 2. retrieveSteps 相关测试 ====================

    @Test
    void testRetrieveSteps_Success() {
        try (var tenantMock = mockStatic(TenantContext.class);
             var securityMock = mockStatic(SecurityUtils.class)) {

            tenantMock.when(TenantContext::get).thenReturn(TENANT_ID);
            securityMock.when(() -> SecurityUtils.isAdmin(anyLong())).thenReturn(false);

            Task task = createDefaultTask();
            Template template = createDefaultTemplate();
            TAppLibrary app = createDefaultApp();
            TAppParam param = new TAppParam();
            param.setAppId("10");
            param.setParamKey("timeout");
            param.setDefaultValue("30");
            param.setParamType("integer");

            when(templateRepository.findById(TEMPLATE_ID)).thenReturn(Optional.of(template));
            when(appLibraryService.selectTAppLibraryById(10L)).thenReturn(app);
            when(appParamService.selectTAppParamList(any())).thenReturn(List.of(param));
            when(constraintService.selectTAppConstraintList(any())).thenReturn(new ArrayList<>());
            when(stepReuseService.getStandardSteps(any(), anyMap(), anyMap())).thenReturn(new ArrayList<>());

            List<TaskStep> result = taskService.retrieveSteps(task);

            assertThat(result).isNotNull();
            verify(stepReuseService).getStandardSteps(any(), anyMap(), anyMap());
        }
    }

    @Test
    void testRetrieveSteps_InvalidFormContent_ThrowsException() {
        try (var tenantMock = mockStatic(TenantContext.class);
             var securityMock = mockStatic(SecurityUtils.class)) {

            tenantMock.when(TenantContext::get).thenReturn(TENANT_ID);
            securityMock.when(() -> SecurityUtils.isAdmin(anyLong())).thenReturn(false);

            Task task = createDefaultTask();
            task.setFormContent("invalid json");
            Template template = createDefaultTemplate();
            TAppLibrary app = createDefaultApp();

            when(templateRepository.findById(TEMPLATE_ID)).thenReturn(Optional.of(template));
            when(appLibraryService.selectTAppLibraryById(10L)).thenReturn(app);
            when(appParamService.selectTAppParamList(any())).thenReturn(new ArrayList<>());
            when(constraintService.selectTAppConstraintList(any())).thenReturn(new ArrayList<>());

            assertThatThrownBy(() -> taskService.retrieveSteps(task))
                    .isInstanceOf(TaskmgtException.class);
        }
    }

    // ==================== 3. updateTask 相关测试 ====================

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
            when(taskRepository.update(any(Task.class))).thenReturn(new ArrayList<>());

            taskService.updateTask(updateTask);

            ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
            verify(taskRepository).update(captor.capture());
            assertThat(captor.getValue().getName()).isEqualTo("更新后的名称");
            verify(redisUtil).deleteObject(anyList());
        }
    }

    @Test
    void testUpdateTask_RobotChanged_SingleTask() {
        try (var securityMock = mockStatic(SecurityUtils.class)) {
            securityMock.when(SecurityUtils::getUsername).thenReturn(TEST_USER);

            Task existingTask = createDefaultTask();
            existingTask.setStatus(Task.NOTSTART);
            existingTask.setRobotId(ROBOT_ID);
            Task updateTask = createDefaultTask();
            updateTask.setRobotId(500L); // 更换机器人

            when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(existingTask));
            when(robotWarningsService.countUnresolvedByRobotId(500L)).thenReturn(0);
            when(stepRepository.findStepsByTaskId(TASK_ID)).thenReturn(new ArrayList<>());
            when(taskRepository.update(any(Task.class))).thenReturn(new ArrayList<>());

            taskService.updateTask(updateTask);

            verify(stepRepository).findStepsByTaskId(TASK_ID);
        }
    }

    @Test
    void testUpdateTask_RobotChanged_GroupTask() {
        try (var securityMock = mockStatic(SecurityUtils.class)) {
            securityMock.when(SecurityUtils::getUsername).thenReturn(TEST_USER);

            Task existingTask = createDefaultTask();
            existingTask.setStatus(Task.NOTSTART);
            existingTask.setIsGroupTask(1);
            existingTask.setRobotGroupId(ROBOT_GROUP_ID);
            Task updateTask = createDefaultTask();
            updateTask.setIsGroupTask(1);
            updateTask.setRobotGroupId(500L); // 更换组

            when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(existingTask));
            when(stepRepository.findStepsByTaskId(TASK_ID)).thenReturn(new ArrayList<>());
            when(taskRepository.update(any(Task.class))).thenReturn(new ArrayList<>());

            taskService.updateTask(updateTask);

            verify(stepRepository).findStepsByTaskId(TASK_ID);
        }
    }

    @Test
    void testUpdateTask_RobotAbnormal_ThrowsException() {
        try (var securityMock = mockStatic(SecurityUtils.class);
             var tenantMock = mockStatic(TenantContext.class)) {
            tenantMock.when(TenantContext::get).thenReturn(TENANT_ID);
            securityMock.when(SecurityUtils::getUsername).thenReturn(TEST_USER);
            securityMock.when(() -> SecurityUtils.isAdmin(anyLong())).thenReturn(false);

            Task existingTask = createDefaultTask();
            existingTask.setStatus(Task.NOTSTART);
            Task updateTask = createDefaultTask();
            updateTask.setRobotId(ROBOT_ID);

            when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(existingTask));
            when(robotWarningsService.countUnresolvedByRobotId(ROBOT_ID)).thenReturn(1); // 有预警

            assertThatThrownBy(() -> taskService.updateTask(updateTask))
                    .isInstanceOf(TaskmgtException.class);
        }
    }

    @Test
    void testUpdateTask_WhenExecuting_ThrowsException() {
        Task updateTask = createDefaultTask();
        updateTask.setStatus(Task.EXECUTING);

        assertThatThrownBy(() -> taskService.updateTask(updateTask))
                .isInstanceOf(TaskmgtException.class);
    }

    @Test
    void testUpdateTask_FromPaused_WithRobotChange() {
        try (var securityMock = mockStatic(SecurityUtils.class)) {
            securityMock.when(SecurityUtils::getUsername).thenReturn(TEST_USER);

            Task existingTask = createDefaultTask();
            existingTask.setStatus(Task.PAUSED);
            existingTask.setRobotId(ROBOT_ID);
            Task updateTask = createDefaultTask();
            updateTask.setStatus(Task.NOTSTART);
            updateTask.setRobotId(500L); // 更换机器人

            when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(existingTask));
            when(robotWarningsService.countUnresolvedByRobotId(500L)).thenReturn(0);
            when(stepRepository.findStepsByTaskId(TASK_ID)).thenReturn(new ArrayList<>());
            when(taskRepository.update(any(Task.class))).thenReturn(new ArrayList<>());

            taskService.updateTask(updateTask);

            ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
            verify(taskRepository).update(captor.capture());
            assertThat(captor.getValue().getStatus()).isEqualTo(Task.PENDING);
        }
    }

    @Test
    void testUpdateTask_WithFormContentAndDisabled() {
        try (var securityMock = mockStatic(SecurityUtils.class);
             var tenantMock = mockStatic(TenantContext.class)) {
            tenantMock.when(TenantContext::get).thenReturn(TENANT_ID);
            securityMock.when(SecurityUtils::getUsername).thenReturn(TEST_USER);
            securityMock.when(() -> SecurityUtils.isAdmin(anyLong())).thenReturn(false);

            Task existingTask = createDefaultTask();
            existingTask.setStatus(Task.DISABLED);
            Task updateTask = createDefaultTask();
            updateTask.setStatus(Task.NOTSTART);
            updateTask.setFormContent("{\"field1\":\"value1\"}");

            Template template = createDefaultTemplate();
            TAppLibrary app = createDefaultApp();

            when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(existingTask));
            when(templateRepository.findById(TEMPLATE_ID)).thenReturn(Optional.of(template));
            when(appLibraryService.selectTAppLibraryById(10L)).thenReturn(app);
            when(appParamService.selectTAppParamList(any())).thenReturn(new ArrayList<>());
            when(constraintService.selectTAppConstraintList(any())).thenReturn(new ArrayList<>());
            when(stepReuseService.getStandardSteps(any(), anyMap(), anyMap())).thenReturn(new ArrayList<>());
            stepService.updateSteps(any(), anyList());
            when(taskRepository.update(any(Task.class))).thenReturn(new ArrayList<>());

            taskService.updateTask(updateTask);

            verify(stepService).updateSteps(eq(TASK_ID), anyList());
        }
    }

    // ==================== 4. deleteTask 相关测试 ====================

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
    void testDeleteTask_WithTemplate() {
        try (var tenantMock = mockStatic(TenantContext.class);
             var securityMock = mockStatic(SecurityUtils.class)) {
            tenantMock.when(TenantContext::get).thenReturn(TENANT_ID);
            securityMock.when(SecurityUtils::getUsername).thenReturn(TEST_USER);
            securityMock.when(() -> SecurityUtils.isAdmin(anyLong())).thenReturn(false);

            Task task = createDefaultTask();
            task.setStatus(Task.DISABLED);
            task.setTemplateId(TEMPLATE_ID);
            when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(task));
            when(taskRepository.update(any(Task.class))).thenReturn(new ArrayList<>());
            when(stepRepository.deleteStepsByTaskId(TASK_ID)).thenReturn(Set.of("step:key"));

            taskService.deleteTask(TASK_ID);

            verify(stepRepository).deleteStepsByTaskId(TASK_ID);
            //verify(redisUtil).deleteObject((anyList()) argThat(list -> list.contains("step:key")));
        }
    }

    // ==================== 5. banTask 相关测试 ====================

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
    void testBanTask_TaskNotFound_ThrowsException() {
        when(taskRepository.findById(TASK_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.banTask(TASK_ID))
                .isInstanceOf(TaskmgtException.class);
    }

    // ==================== 6. resumeTask 相关测试 ====================

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
    void testResumeTask_NotDisabled_ThrowsException() {
        Task task = createDefaultTask();
        task.setStatus(Task.NOTSTART);
        when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(task));

        assertThatThrownBy(() -> taskService.resumeTask(TASK_ID))
                .isInstanceOf(TaskmgtException.class);
    }

    // ==================== 7. pauseTask 相关测试 ====================

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
            when(stepReuseService.pauseStepsByTask(any(Task.class))).thenReturn(new ArrayList<>());

            taskService.pauseTask(TASK_ID);

            verify(stepReuseService).pauseStepsByTask(any(Task.class));
            verify(taskLogService).record(eq(TASK_ID), isNull(), eq(TaskLogEventType.TASK_PAUSE),
                    anyString(), eq(TEST_USER), eq(TENANT_ID));
        }
    }

    @Test
    void testPauseTask_WithoutTemplate() {
        try (var tenantMock = mockStatic(TenantContext.class);
             var securityMock = mockStatic(SecurityUtils.class)) {
            tenantMock.when(TenantContext::get).thenReturn(TENANT_ID);
            securityMock.when(SecurityUtils::getUsername).thenReturn(TEST_USER);
            securityMock.when(() -> SecurityUtils.isAdmin(anyLong())).thenReturn(false);

            Task task = createDefaultTask();
            task.setStatus(Task.EXECUTING);
            task.setTemplateId(null);
            when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(task));
            when(taskRepository.update(any(Task.class))).thenReturn(new ArrayList<>());

            taskService.pauseTask(TASK_ID);

            verify(stepReuseService, never()).pauseStepsByTask(any());
        }
    }

    // ==================== 8. continueTask 相关测试 ====================

    @Test
    void testContinueTask_WhenRobotAvailable() {
        try (var tenantMock = mockStatic(TenantContext.class);
             var securityMock = mockStatic(SecurityUtils.class)) {
            tenantMock.when(TenantContext::get).thenReturn(TENANT_ID);
            securityMock.when(SecurityUtils::getUsername).thenReturn(TEST_USER);
            securityMock.when(() -> SecurityUtils.isAdmin(anyLong())).thenReturn(false);

            Task task = createDefaultTask();
            task.setStatus(Task.PAUSED);
            task.setIsGroupTask(0);
            task.setRobotId(ROBOT_ID);
            when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(task));

            Robot robot = createDefaultRobot();
            when(robotService.selectRobotsById(ROBOT_ID)).thenReturn(robot);
            when(robotWarningsService.countUnresolvedByRobotId(ROBOT_ID)).thenReturn(0);
            when(stepRepository.countByAssignedRobotIdAndTaskIdAndStatusIn(eq(ROBOT_ID), isNull(), anyList())).thenReturn(0L);
            when(taskRepository.findByRobotIdAndStatus(ROBOT_ID, Task.EXECUTING)).thenReturn(Collections.emptyList());

            when(taskRepository.update(any(Task.class))).thenReturn(new ArrayList<>());
            //when(stepReuseService.continueStepsByTaskId(TASK_ID)).thenReturn(new ArrayList<>());

            taskService.continueTask(TASK_ID);

            ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
            verify(taskRepository).update(captor.capture());
            assertThat(captor.getValue().getStatus()).isEqualTo(Task.EXECUTING);
            //verify(stepReuseService).continueStepsByTaskId(TASK_ID);
        }
    }

    @Test
    void testContinueTask_WhenRobotNotAvailable() {
        try (var tenantMock = mockStatic(TenantContext.class);
             var securityMock = mockStatic(SecurityUtils.class)) {
            tenantMock.when(TenantContext::get).thenReturn(TENANT_ID);
            securityMock.when(SecurityUtils::getUsername).thenReturn(TEST_USER);
            securityMock.when(() -> SecurityUtils.isAdmin(anyLong())).thenReturn(false);

            Task task = createDefaultTask();
            task.setStatus(Task.PAUSED);
            task.setIsGroupTask(0);
            task.setRobotId(ROBOT_ID);
            when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(task));

            Robot robot = createDefaultRobot();
            when(robotService.selectRobotsById(ROBOT_ID)).thenReturn(robot);
            when(robotWarningsService.countUnresolvedByRobotId(ROBOT_ID)).thenReturn(1); // 有预警

            when(taskRepository.update(any(Task.class))).thenReturn(new ArrayList<>());

            taskService.continueTask(TASK_ID);

            ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
            verify(taskRepository).update(captor.capture());
            assertThat(captor.getValue().getStatus()).isEqualTo(Task.PENDING);
            verify(taskLogService).record(eq(TASK_ID), isNull(), eq(TaskLogEventType.TASK_PENDING),
                    anyString(), eq(TEST_USER), eq(TENANT_ID));
            verify(stepReuseService, never()).continueStepsByTask(any());
        }
    }

    @Test
    void testContinueTask_GroupTask_RobotAvailable() {
        try (var tenantMock = mockStatic(TenantContext.class);
             var securityMock = mockStatic(SecurityUtils.class)) {
            tenantMock.when(TenantContext::get).thenReturn(TENANT_ID);
            securityMock.when(SecurityUtils::getUsername).thenReturn(TEST_USER);
            securityMock.when(() -> SecurityUtils.isAdmin(anyLong())).thenReturn(false);

            Task task = createDefaultTask();
            task.setStatus(Task.PAUSED);
            task.setIsGroupTask(1);
            task.setRobotGroupId(ROBOT_GROUP_ID);
            when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(task));

            Robot robot1 = createDefaultRobot();
            robot1.setId(301L);
            Robot robot2 = createDefaultRobot();
            robot2.setId(302L);
            when(robotService.selectRobotsList(any(Robot.class))).thenReturn(List.of(robot1, robot2));
            when(robotWarningsService.countUnresolvedByRobotId(301L)).thenReturn(1); // 异常
            when(robotWarningsService.countUnresolvedByRobotId(302L)).thenReturn(0); // 正常
            when(taskRepository.findByRobotIdAndStatus(302L, Task.EXECUTING)).thenReturn(Collections.emptyList());
            when(stepRepository.countByAssignedRobotIdAndTaskIdAndStatusIn(eq(302L), isNull(), anyList())).thenReturn(0L);

            when(taskRepository.update(any(Task.class))).thenReturn(new ArrayList<>());
           // when(stepReuseService.continueStepsByTaskId(TASK_ID)).thenReturn(new ArrayList<>());

            taskService.continueTask(TASK_ID);

            ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
            verify(taskRepository).update(captor.capture());
            assertThat(captor.getValue().getStatus()).isEqualTo(Task.EXECUTING);
        }
    }

    // ==================== 9. terminateTask 相关测试 ====================

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
            //when(stepReuseService.terminatedStepsByTaskId(TASK_ID)).thenReturn(new ArrayList<>());

            taskService.terminateTask(TASK_ID, "手动终止");

            ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
            verify(taskRepository).update(captor.capture());
            assertThat(captor.getValue().getStatus()).isEqualTo(Task.TERMINATED);
            assertThat(captor.getValue().getTerminateReason()).isEqualTo("手动终止");
        }
    }

    @Test
    void testTerminateTask_WithoutTemplate() {
        try (var tenantMock = mockStatic(TenantContext.class);
             var securityMock = mockStatic(SecurityUtils.class)) {
            tenantMock.when(TenantContext::get).thenReturn(TENANT_ID);
            securityMock.when(SecurityUtils::getUsername).thenReturn(TEST_USER);
            securityMock.when(() -> SecurityUtils.isAdmin(anyLong())).thenReturn(false);

            Task task = createDefaultTask();
            task.setStatus(Task.EXECUTING);
            task.setTemplateId(null);
            when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(task));
            when(taskRepository.update(any(Task.class))).thenReturn(new ArrayList<>());

            taskService.terminateTask(TASK_ID, "手动终止");

            verify(stepReuseService, never()).terminatedStepsByTask(any());
        }
    }

    // ==================== 10. cancelTask 相关测试 ====================

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

    // ==================== 11. resolveRisk 相关测试 ====================

    @Test
    void testResolveRisk_Success_SingleTask() {
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
    void testResolveRisk_GroupTask_WithWarnings() {
        try (var tenantMock = mockStatic(TenantContext.class);
             var securityMock = mockStatic(SecurityUtils.class)) {
            tenantMock.when(TenantContext::get).thenReturn(TENANT_ID);
            securityMock.when(SecurityUtils::getUsername).thenReturn(TEST_USER);
            securityMock.when(() -> SecurityUtils.isAdmin(anyLong())).thenReturn(false);

            Task task = createDefaultTask();
            task.setStatus(Task.EXECUTING);
            task.setIsGroupTask(1);
            task.setRobotGroupId(ROBOT_GROUP_ID);
            task.setRiskLevel(2);

            TaskStep step1 = new TaskStep();
            step1.setAssignedRobotId(301L);
            TaskStep step2 = new TaskStep();
            step2.setAssignedRobotId(302L);

            when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(task));
            when(stepRepository.findStepsByTaskId(TASK_ID)).thenReturn(List.of(step1, step2));
            when(robotWarningsService.countUnresolvedByRobotId(301L)).thenReturn(0);
            when(robotWarningsService.countUnresolvedByRobotId(302L)).thenReturn(1); // 有预警
//            when(taskRepository.update(any(Task.class))).thenReturn(new ArrayList<>());

            boolean result = taskService.resolveRisk(TASK_ID);

            assertThat(result).isFalse();
        }
    }

    @Test
    void testResolveRisk_TaskDeleted_ThrowsException() {
        try (var tenantMock = mockStatic(TenantContext.class);
             var securityMock = mockStatic(SecurityUtils.class)) {
            tenantMock.when(TenantContext::get).thenReturn(TENANT_ID);
            securityMock.when(() -> SecurityUtils.isAdmin(anyLong())).thenReturn(false);

            Task task = createDefaultTask();
            task.setStatus(Task.DELETED);
            when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(task));

            assertThatThrownBy(() -> taskService.resolveRisk(TASK_ID))
                    .isInstanceOf(TaskmgtException.class);
        }
    }

    @Test
    void testResolveRisk_NotAllNormal_Executing() {
        try (var tenantMock = mockStatic(TenantContext.class);
             var securityMock = mockStatic(SecurityUtils.class)) {
            tenantMock.when(TenantContext::get).thenReturn(TENANT_ID);
            securityMock.when(SecurityUtils::getUsername).thenReturn(TEST_USER);
            securityMock.when(() -> SecurityUtils.isAdmin(anyLong())).thenReturn(false);

            Task task = createDefaultTask();
            task.setStatus(Task.EXECUTING);
            task.setIsGroupTask(0);
            task.setRobotId(ROBOT_ID);
            when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(task));
            when(robotWarningsService.countUnresolvedByRobotId(ROBOT_ID)).thenReturn(1);

            boolean result = taskService.resolveRisk(TASK_ID);

            assertThat(result).isFalse();
        }
    }

    // ==================== 12. retrieveTasks 相关测试 ====================

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
    void testRetrieveTasks_WithRobotId() {
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

            Robot robot = createDefaultRobot();
            robot.setGroupId(ROBOT_GROUP_ID);

            // 第一次查询返回有数据
            when(taskRepository.getTasks(isNull(), isNull(), isNull(), eq(ROBOT_ID), isNull(),
                    isNull(), isNull(), isNull(), eq(TENANT_ID)))
                    .thenReturn(new ArrayList<>(List.of(task1)));

            when(robotService.selectRobotsById(ROBOT_ID)).thenReturn(robot);

            // 通过 groupId 查询
            when(taskRepository.getTasks(isNull(), isNull(), isNull(), isNull(), eq(ROBOT_GROUP_ID),
                    isNull(), isNull(), isNull(), eq(TENANT_ID)))
                    .thenReturn(new ArrayList<>(List.of(task2)));

            when(templateRepository.getTemplateNameById(any())).thenReturn("模板A");
            when(stepRepository.findStepsByTaskId(anyLong())).thenReturn(new ArrayList<>());

            List<TaskVo> result = taskService.retrieveTasks(null, null, null, ROBOT_ID, null, null, null, null);

            assertThat(result).hasSize(2);  // task1 + task2
        }
    }

    @Test
    void testRetrieveTasks_WithRobotGroupId() {
        try (var tenantMock = mockStatic(TenantContext.class);
             var securityMock = mockStatic(SecurityUtils.class)) {
            tenantMock.when(TenantContext::get).thenReturn(TENANT_ID);
            securityMock.when(() -> SecurityUtils.isAdmin(anyLong())).thenReturn(false);

            Task task = createDefaultTask();
            task.setId(1L);

            // 第一次查询返回有数据
            when(taskRepository.getTasks(isNull(), isNull(), isNull(), isNull(), eq(ROBOT_GROUP_ID),
                    isNull(), isNull(), isNull(), eq(TENANT_ID)))
                    .thenReturn(new ArrayList<>(List.of(task)));

            when(templateRepository.getTemplateNameById(any())).thenReturn("模板A");
            when(stepRepository.findStepsByTaskId(anyLong())).thenReturn(new ArrayList<>());

            List<TaskVo> result = taskService.retrieveTasks(null, null, null, null, ROBOT_GROUP_ID, null, null, null);

            assertThat(result).hasSize(1);
        }
    }
    @Test
    void testRetrieveTasks_EmptyResult() {
        try (var tenantMock = mockStatic(TenantContext.class);
             var securityMock = mockStatic(SecurityUtils.class)) {
            tenantMock.when(TenantContext::get).thenReturn(TENANT_ID);
            securityMock.when(() -> SecurityUtils.isAdmin(anyLong())).thenReturn(false);

            when(taskRepository.getTasks(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                    .thenReturn(new ArrayList<>());

            List<TaskVo> result = taskService.retrieveTasks(Task.NOTSTART, null, null, null, null, null, null, null);

            assertThat(result).isEmpty();
        }
    }

    // ==================== 13. getTask 相关测试 ====================

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
    void testGetTask_WithGroup() {
        Task task = createDefaultTask();
        task.setRobotId(null);
        task.setRobotGroupId(ROBOT_GROUP_ID);
        when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(task));
        when(robotGroupsService.selectRobotGroupsById(ROBOT_GROUP_ID)).thenReturn(
                new com.ruoyi.robots.domain.RobotGroups() {{
                    setName("测试组");
                }}
        );

        TaskVo vo = taskService.getTask(TASK_ID);

        assertThat(vo.getRobotGroupName()).isEqualTo("测试组");
    }

    @Test
    void testGetTask_NotFound_ThrowsException() {
        when(taskRepository.findById(TASK_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.getTask(TASK_ID))
                .isInstanceOf(TaskmgtException.class);
    }

    // ==================== 14. getAbnormalTask 相关测试 ====================

    @Test
    void testGetAbnormalTask_Success() {
        Task task = createDefaultTask();
        task.setRiskLevel(2);
        when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(task));
        when(templateRepository.getTemplateNameById(TEMPLATE_ID)).thenReturn("模板A");
        when(robotService.selectRobotsById(ROBOT_ID)).thenReturn(createDefaultRobot());
        when(robotWarningsService.selectRobotWarningsList(any())).thenReturn(new ArrayList<>());

        TaskAbnormalVo vo = taskService.getAbnormalTask(TASK_ID);

        assertThat(vo).isNotNull();
        assertThat(vo.getRiskLevel()).isEqualTo(2);
    }

    // ==================== 15. updateGlobalOrder 相关测试 ====================

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
    void testUpdateGlobalOrder_InvalidSize_ThrowsException() {
        try (var tenantMock = mockStatic(TenantContext.class);
             var securityMock = mockStatic(SecurityUtils.class)) {
            tenantMock.when(TenantContext::get).thenReturn(TENANT_ID);
            securityMock.when(() -> SecurityUtils.isAdmin(anyLong())).thenReturn(false);

            Task taskA = createDefaultTask();
            taskA.setId(1L);
            taskA.setStatus(Task.PENDING);

            when(taskRepository.getTasks(eq(Task.PENDING), isNull(), isNull(), isNull(), isNull(),
                    isNull(), isNull(), isNull(), any())).thenReturn(List.of(taskA));

            assertThatThrownBy(() -> taskService.updateGlobalOrder(List.of(1L, 2L)))
                    .isInstanceOf(TaskmgtException.class);
        }
    }

    @Test
    void testUpdateGlobalOrder_WrongOrder_ThrowsException() {
        try (var tenantMock = mockStatic(TenantContext.class);
             var securityMock = mockStatic(SecurityUtils.class)) {
            tenantMock.when(TenantContext::get).thenReturn(TENANT_ID);
            securityMock.when(() -> SecurityUtils.isAdmin(anyLong())).thenReturn(false);

            Task taskA = createDefaultTask();
            taskA.setId(1L);
            taskA.setStatus(Task.PENDING);
            taskA.setIsGroupTask(0);
            taskA.setRobotId(10L);
            taskA.setPendingOrder(0);

            Task taskB = createDefaultTask();
            taskB.setId(2L);
            taskB.setStatus(Task.PENDING);
            taskB.setIsGroupTask(0);
            taskB.setRobotId(10L);
            taskB.setPendingOrder(1);

            when(taskRepository.getTasks(eq(Task.PENDING), isNull(), isNull(), isNull(), isNull(),
                    isNull(), isNull(), isNull(), any())).thenReturn(List.of(taskA, taskB));

            // 传入顺序与 pendingOrder 不一致
            assertThatThrownBy(() -> taskService.updateGlobalOrder(List.of(2L, 1L)))
                    .isInstanceOf(TaskmgtException.class);
        }
    }
    @Test
    void testUpdateGlobalOrder_EmptyList() {
        taskService.updateGlobalOrder(new ArrayList<>());
        verify(taskRepository, never()).update(any());
    }

    // ==================== 16. updateLocalOrder 相关测试 ====================

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
    void testUpdateLocalOrder_InvalidIds_ThrowsException() {
        try (var tenantMock = mockStatic(TenantContext.class);
             var securityMock = mockStatic(SecurityUtils.class)) {
            tenantMock.when(TenantContext::get).thenReturn(TENANT_ID);
            securityMock.when(() -> SecurityUtils.isAdmin(anyLong())).thenReturn(false);

            when(taskRepository.getTasks(eq(Task.PENDING), eq(0), isNull(), eq(10L), isNull(),
                    isNull(), isNull(), isNull(), any())).thenReturn(new ArrayList<>());

            assertThatThrownBy(() -> taskService.updateLocalOrder(10L, false, List.of(1L)))
                    .isInstanceOf(TaskmgtException.class);
        }
    }

    @Test
    void testUpdateLocalOrder_EmptyList() {
        taskService.updateLocalOrder(10L, false, new ArrayList<>());
        verify(taskRepository, never()).update(any());
    }

    // ==================== 17. getAbnormalTasks 相关测试 ====================

    @Test
    void testGetAbnormalTasks_ByRiskLevel() {
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

    @Test
    void testGetAbnormalTasks_AllRiskLevels() {
        try (var tenantMock = mockStatic(TenantContext.class);
             var securityMock = mockStatic(SecurityUtils.class)) {
            tenantMock.when(TenantContext::get).thenReturn(TENANT_ID);
            securityMock.when(() -> SecurityUtils.isAdmin(anyLong())).thenReturn(false);

            Task task1 = createDefaultTask();
            task1.setRiskLevel(1);
            task1.setIsGroupTask(0);
            task1.setRobotId(ROBOT_ID);

            Task task2 = createDefaultTask();
            task2.setRiskLevel(2);
            task2.setIsGroupTask(0);
            task2.setRobotId(ROBOT_ID);

            // 使用 ArrayList 替代 List.of
            when(taskRepository.getTasks(isNull(), isNull(), isNull(), isNull(), isNull(),
                    isNull(), eq(1), isNull(), eq(TENANT_ID)))
                    .thenReturn(new ArrayList<>(List.of(task1)));
            when(taskRepository.getTasks(isNull(), isNull(), isNull(), isNull(), isNull(),
                    isNull(), eq(2), isNull(), eq(TENANT_ID)))
                    .thenReturn(new ArrayList<>(List.of(task2)));

            // mock robotService
            Robot robot = createDefaultRobot();
            when(robotService.selectRobotsById(ROBOT_ID)).thenReturn(robot);
            when(robotWarningsService.selectRobotWarningsList(any())).thenReturn(new ArrayList<>());

            var result = taskService.getAbnormalTasks(null, null, null);
            assertThat(result).hasSize(2);
        }
    }
    @Test
    void testGetAbnormalTasks_GroupTask() {
        try (var tenantMock = mockStatic(TenantContext.class);
             var securityMock = mockStatic(SecurityUtils.class)) {
            tenantMock.when(TenantContext::get).thenReturn(TENANT_ID);
            securityMock.when(() -> SecurityUtils.isAdmin(anyLong())).thenReturn(false);

            Task task = createDefaultTask();
            task.setRiskLevel(2);
            task.setIsGroupTask(1);
            task.setRobotGroupId(ROBOT_GROUP_ID);

            TaskStep step1 = new TaskStep();
            step1.setAssignedRobotId(301L);
            TaskStep step2 = new TaskStep();
            step2.setAssignedRobotId(302L);

            when(taskRepository.getTasks(isNull(), isNull(), isNull(), isNull(), eq(ROBOT_GROUP_ID),
                    isNull(), eq(2), isNull(), any())).thenReturn(List.of(task));
            when(stepRepository.findStepsByTaskId(TASK_ID)).thenReturn(List.of(step1, step2));

            Robot robot1 = createDefaultRobot();
            robot1.setId(301L);
            Robot robot2 = createDefaultRobot();
            robot2.setId(302L);
            when(robotService.selectRobotsById(301L)).thenReturn(robot1);
            when(robotService.selectRobotsById(302L)).thenReturn(robot2);
            when(robotWarningsService.selectRobotWarningsList(any())).thenReturn(new ArrayList<>());
            when(robotGroupsService.selectRobotGroupsById(ROBOT_GROUP_ID)).thenReturn(
                    new com.ruoyi.robots.domain.RobotGroups() {{
                        setName("测试组");
                    }}
            );
            when(stepRepository.findStepsByTaskId(TASK_ID)).thenReturn(List.of(step1, step2));

            var result = taskService.getAbnormalTasks(2, null, ROBOT_GROUP_ID);
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getRobotGroupName()).isEqualTo("测试组");
        }
    }

    @Test
    void testGetAbnormalTasks_GroupTask_WithWarning() {
        try (var tenantMock = mockStatic(TenantContext.class);
             var securityMock = mockStatic(SecurityUtils.class)) {
            tenantMock.when(TenantContext::get).thenReturn(TENANT_ID);
            securityMock.when(() -> SecurityUtils.isAdmin(anyLong())).thenReturn(false);

            Task task = createDefaultTask();
            task.setRiskLevel(2);
            task.setIsGroupTask(1);
            task.setRobotGroupId(ROBOT_GROUP_ID);

            TaskStep step = new TaskStep();
            step.setAssignedRobotId(301L);

            RobotWarnings warning = new RobotWarnings();
            warning.setWarningType("0");
            warning.setWarningLevel("1");

            when(taskRepository.getTasks(isNull(), isNull(), isNull(), isNull(), eq(ROBOT_GROUP_ID),
                    isNull(), eq(2), isNull(), any())).thenReturn(List.of(task));
            when(stepRepository.findStepsByTaskId(TASK_ID)).thenReturn(List.of(step));
            Robot robot = createDefaultRobot();
            robot.setId(301L);
            when(robotService.selectRobotsById(301L)).thenReturn(robot);
            when(robotWarningsService.selectRobotWarningsList(any())).thenReturn(List.of(warning));
            when(robotGroupsService.selectRobotGroupsById(ROBOT_GROUP_ID)).thenReturn(
                    new com.ruoyi.robots.domain.RobotGroups() {{
                        setName("测试组");
                    }}
            );

            var result = taskService.getAbnormalTasks(2, null, ROBOT_GROUP_ID);
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getRobotStatusSummary()).contains("异常");
        }
    }

    @Test
    void testGetAbnormalTasks_RiskLevelZero() {
        try (var tenantMock = mockStatic(TenantContext.class);
             var securityMock = mockStatic(SecurityUtils.class)) {
            tenantMock.when(TenantContext::get).thenReturn(TENANT_ID);
            securityMock.when(() -> SecurityUtils.isAdmin(anyLong())).thenReturn(false);

            var result = taskService.getAbnormalTasks(0, null, null);
            assertThat(result).isEmpty();
        }
    }

    // ==================== 18. isRobotNormal 相关测试 ====================

    @Test
    void testIsRobotNormal_SingleTask_Normal() {
        Task task = createDefaultTask();
        when(robotWarningsService.countUnresolvedByRobotId(ROBOT_ID)).thenReturn(0);

        boolean result = taskService.isRobotNormal(task);

        assertThat(result).isTrue();
    }

    @Test
    void testIsRobotNormal_SingleTask_Abnormal() {
        Task task = createDefaultTask();
        when(robotWarningsService.countUnresolvedByRobotId(ROBOT_ID)).thenReturn(1);

        boolean result = taskService.isRobotNormal(task);

        assertThat(result).isFalse();
    }

    @Test
    void testIsRobotNormal_GroupTask_Normal() {
        Task task = createDefaultTask();
        task.setRobotId(null);
        task.setRobotGroupId(ROBOT_GROUP_ID);

        Robot robot1 = createDefaultRobot();
        robot1.setId(301L);
        Robot robot2 = createDefaultRobot();
        robot2.setId(302L);
        when(robotService.selectRobotsList(any(Robot.class))).thenReturn(List.of(robot1, robot2));
        when(robotWarningsService.countUnresolvedByRobotId(301L)).thenReturn(1);
        when(robotWarningsService.countUnresolvedByRobotId(302L)).thenReturn(0);

        boolean result = taskService.isRobotNormal(task);

        assertThat(result).isTrue();
    }

    @Test
    void testIsRobotNormal_GroupTask_AllAbnormal() {
        Task task = createDefaultTask();
        task.setRobotId(null);
        task.setRobotGroupId(ROBOT_GROUP_ID);

        Robot robot = createDefaultRobot();
        when(robotService.selectRobotsList(any(Robot.class))).thenReturn(List.of(robot));
        when(robotWarningsService.countUnresolvedByRobotId(ROBOT_ID)).thenReturn(1);

        boolean result = taskService.isRobotNormal(task);

        assertThat(result).isFalse();
    }

    @Test
    void testIsRobotNormal_NoRobot() {
        Task task = createDefaultTask();
        task.setRobotId(null);
        task.setRobotGroupId(null);

        boolean result = taskService.isRobotNormal(task);

        assertThat(result).isTrue();
    }
}