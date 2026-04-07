package com.ruoyi.taskmgt.service.impl;

import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.exception.task.TaskmgtException;
import com.ruoyi.common.threadlocal.TenantContext;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.taskmgt.domain.StepRepository;
import com.ruoyi.taskmgt.domain.TaskRepository;
import com.ruoyi.taskmgt.domain.bo.Task;
import com.ruoyi.taskmgt.domain.bo.TaskStep;
import com.ruoyi.taskmgt.service.vo.TaskStepVo;
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
class StepServiceImplTest {

    @Mock private TaskRepository taskRepository;
    @Mock private MessageSourceAccessor messageSourceAccessor;
    @Mock private StepRepository stepRepository;
    @Mock private TaskLogReuseService taskLogService;
    @Mock private RedisCache redisUtil;
    @Mock private TaskReuseService taskReuseService;

    @InjectMocks
    private StepServiceImpl stepService;

    private static final Long TASK_ID = 100L;
    private static final Long STEP_ID = 200L;
    private static final Long TENANT_ID = 1L;
    private static final String TEST_USER = "testUser";

    private Task createDefaultTask() {
        Task task = new Task();
        task.setId(TASK_ID);
        task.setName("测试任务");
        task.setStatus(Task.EXECUTING);
        return task;
    }

    private TaskStep createDefaultStep() {
        TaskStep step = new TaskStep();
        step.setId(STEP_ID);
        step.setTaskId(TASK_ID);
        step.setStepName("测试步骤");
        step.setOrderNum(1);
        step.setStatus(TaskStep.NOTSTART);
        return step;
    }

    @BeforeEach
    void setUp() {
        lenient().when(messageSourceAccessor.getMessage(anyString())).thenReturn("测试消息");
    }
    @Test
    void testCreateSteps_Success() {
        try (var tenantMock = mockStatic(TenantContext.class);
             var securityMock = mockStatic(SecurityUtils.class)) {
            tenantMock.when(TenantContext::get).thenReturn(TENANT_ID);
            securityMock.when(() -> SecurityUtils.isAdmin(anyLong())).thenReturn(false);

            Task task = createDefaultTask();
            when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(task));

            List<TaskStep> steps = List.of(createDefaultStep());
            when(stepRepository.insert(any(TaskStep.class))).thenAnswer(inv -> {
                TaskStep arg = inv.getArgument(0);
                arg.setId(STEP_ID);
                return arg;
            });

            List<TaskStepVo> result = stepService.createSteps(TASK_ID, steps);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getId()).isEqualTo(STEP_ID);
            assertThat(result.get(0).getTaskName()).isEqualTo("测试任务");
            verify(stepRepository, times(1)).insert(any(TaskStep.class));
        }
    }

    @Test
    void testCreateSteps_TaskNotFound_ThrowsException() {
        try (var tenantMock = mockStatic(TenantContext.class);
             var securityMock = mockStatic(SecurityUtils.class)) {
            tenantMock.when(TenantContext::get).thenReturn(TENANT_ID);
            securityMock.when(() -> SecurityUtils.isAdmin(anyLong())).thenReturn(false);

            when(taskRepository.findById(TASK_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> stepService.createSteps(TASK_ID, List.of(createDefaultStep())))
                    .isInstanceOf(TaskmgtException.class);
        }
    }

    @Test
    void testCreateSteps_EmptySteps_ReturnsEmptyList() {
        try (var tenantMock = mockStatic(TenantContext.class);
             var securityMock = mockStatic(SecurityUtils.class)) {
            tenantMock.when(TenantContext::get).thenReturn(TENANT_ID);
            securityMock.when(() -> SecurityUtils.isAdmin(anyLong())).thenReturn(false);

            List<TaskStepVo> result = stepService.createSteps(TASK_ID, Collections.emptyList());
            assertThat(result).isEmpty();
            verifyNoInteractions(taskRepository);
        }
    }
    @Test
    void testUpdateSteps_Success() {
        try (var tenantMock = mockStatic(TenantContext.class);
             var securityMock = mockStatic(SecurityUtils.class)) {
            tenantMock.when(TenantContext::get).thenReturn(TENANT_ID);
            securityMock.when(SecurityUtils::getUsername).thenReturn(TEST_USER);
            securityMock.when(() -> SecurityUtils.isAdmin(anyLong())).thenReturn(false);

            Task task = createDefaultTask();
            when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(task));

            TaskStep step = createDefaultStep();
            step.setOrderNum(1);
            TaskStep existingStep = createDefaultStep();
            existingStep.setId(STEP_ID);
            when(stepRepository.findStepByTaskIdAndOrder(TASK_ID, 1)).thenReturn(existingStep);
            when(stepRepository.update(any(TaskStep.class))).thenReturn(List.of("step:key"));

            stepService.updateSteps(TASK_ID, List.of(step));

            ArgumentCaptor<TaskStep> captor = ArgumentCaptor.forClass(TaskStep.class);
            verify(stepRepository).update(captor.capture());
            assertThat(captor.getValue().getId()).isEqualTo(STEP_ID);
            verify(redisUtil).deleteObject(anyList());
        }
    }
    @Test
    void testUpdateSteps_RobotBusy_UpdatesTaskStatus() {
        try (var tenantMock = mockStatic(TenantContext.class);
             var securityMock = mockStatic(SecurityUtils.class)) {
            tenantMock.when(TenantContext::get).thenReturn(TENANT_ID);
            securityMock.when(SecurityUtils::getUsername).thenReturn(TEST_USER);
            securityMock.when(() -> SecurityUtils.isAdmin(anyLong())).thenReturn(false);

            Task task = createDefaultTask();
            task.setStatus(Task.EXECUTING);
            when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(task));

            TaskStep step = createDefaultStep();
            step.setAssignedRobotId(999L);
            when(stepRepository.countByAssignedRobotIdAndTaskIdAndStatusIn(eq(999L), isNull(), anyList())).thenReturn(1L);
            when(stepRepository.findStepByTaskIdAndOrder(TASK_ID, step.getOrderNum())).thenReturn(null);
            when(stepRepository.insert(any(TaskStep.class))).thenReturn(step);
            when(taskRepository.update(any(Task.class))).thenReturn(List.of("task:key"));

            stepService.updateSteps(TASK_ID, List.of(step));

            ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
            verify(taskRepository).update(taskCaptor.capture());
            assertThat(taskCaptor.getValue().getStatus()).isEqualTo(Task.PENDING);
        }
    }
    @Test
    void testRetrieveSteps_Success() {
        Task task = createDefaultTask();
        when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(task));

        TaskStep step = createDefaultStep();
        when(stepRepository.findStepsByTaskId(TASK_ID)).thenReturn(List.of(step));

        List<TaskStepVo> result = stepService.retrieveSteps(TASK_ID);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTaskName()).isEqualTo("测试任务");
    }

    @Test
    void testRetrieveSteps_TaskNotFound_ThrowsException() {
        when(taskRepository.findById(TASK_ID)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> stepService.retrieveSteps(TASK_ID))
                .isInstanceOf(TaskmgtException.class);
    }

    @Test
    void testCompleteStep_Success_WithNextStep() {
        TaskStep step = createDefaultStep();
        step.setStatus(TaskStep.EXECUTING);
        step.setOrderNum(1);
        when(stepRepository.findById(STEP_ID)).thenReturn(Optional.of(step));
        when(stepRepository.update(any(TaskStep.class))).thenReturn(List.of("step:key"));

        TaskStep nextStep = createDefaultStep();
        nextStep.setId(300L);
        nextStep.setOrderNum(2);
        nextStep.setStatus(TaskStep.NOTSTART);
        when(stepRepository.findStepsByTaskId(TASK_ID)).thenReturn(List.of(step, nextStep));

        stepService.completeStep(STEP_ID);

        verify(stepRepository).update(step);
        verify(stepRepository).update(nextStep); // startStep called
        verify(taskLogService, times(2)).record(any(), any(), any(), anyString(), any(), any());
    }

    @Test
    void testCompleteStep_AllStepsCompleted_CompletesTask() {
        TaskStep step = createDefaultStep();
        step.setStatus(TaskStep.EXECUTING);
        step.setOrderNum(1);
        when(stepRepository.findById(STEP_ID)).thenReturn(Optional.of(step));
        when(stepRepository.update(any(TaskStep.class))).thenReturn(List.of("step:key"));

        when(stepRepository.findStepsByTaskId(TASK_ID)).thenReturn(List.of(step)); // only one step

        Task task = createDefaultTask();
        when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(task));

        stepService.completeStep(STEP_ID);

        verify(taskReuseService).completeTask(task);
    }

    @Test
    void testCompleteStep_StepNotFound_ThrowsException() {
        when(stepRepository.findById(STEP_ID)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> stepService.completeStep(STEP_ID))
                .isInstanceOf(TaskmgtException.class);
    }

    @Test
    void testCompleteStep_StepNotExecuting_ThrowsException() {
        TaskStep step = createDefaultStep();
        step.setStatus(TaskStep.NOTSTART);
        when(stepRepository.findById(STEP_ID)).thenReturn(Optional.of(step));
        assertThatThrownBy(() -> stepService.completeStep(STEP_ID))
                .isInstanceOf(TaskmgtException.class);
    }
}