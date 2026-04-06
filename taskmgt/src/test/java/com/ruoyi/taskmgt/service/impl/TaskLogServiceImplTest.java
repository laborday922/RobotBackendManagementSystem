package com.ruoyi.taskmgt.service.impl;
import com.ruoyi.common.exception.task.TaskmgtException;
import com.ruoyi.common.threadlocal.TenantContext;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.taskmgt.domain.StepRepository;
import com.ruoyi.taskmgt.domain.TaskLogRepository;
import com.ruoyi.taskmgt.domain.TaskRepository;
import com.ruoyi.taskmgt.domain.bo.Task;
import com.ruoyi.taskmgt.domain.bo.TaskLog;
import com.ruoyi.taskmgt.domain.bo.TaskStep;
import com.ruoyi.taskmgt.service.vo.TaskLogVo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.support.MessageSourceAccessor;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskLogServiceImplTest {

    @Mock private TaskLogRepository taskLogRepository;
    @Mock private MessageSourceAccessor messageSourceAccessor;
    @Mock private TaskRepository taskRepository;
    @Mock private StepRepository stepRepository;

    @InjectMocks
    private TaskLogServiceImpl taskLogService;

    private static final Long LOG_ID = 100L;
    private static final Long TASK_ID = 200L;
    private static final Long STEP_ID = 300L;

    private TaskLog createDefaultLog() {
        TaskLog log = new TaskLog();
        log.setId(LOG_ID);
        log.setTaskId(TASK_ID);
        log.setStepId(STEP_ID);
        log.setEventType("TEST_EVENT");
        log.setContent("测试内容");
        log.setOperator("testUser");
        return log;
    }

    private Task createDefaultTask() {
        Task task = new Task();
        task.setId(TASK_ID);
        task.setName("测试任务");
        return task;
    }

    private TaskStep createDefaultStep() {
        TaskStep step = new TaskStep();
        step.setId(STEP_ID);
        step.setStepName("测试步骤");
        return step;
    }

    // ==================== queryLogs ====================
    @Test
    void testQueryLogs_Success() {
        try (var tenantMock = mockStatic(TenantContext.class);
             var securityMock = mockStatic(SecurityUtils.class)) {
            tenantMock.when(TenantContext::get).thenReturn(1L);
            securityMock.when(() -> SecurityUtils.isAdmin(anyLong())).thenReturn(false);

            TaskLog log = createDefaultLog();
            when(taskLogRepository.findTaskLogs(any(), any(), any(), any())).thenReturn(List.of(log));
            when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(createDefaultTask()));
            when(stepRepository.findById(STEP_ID)).thenReturn(Optional.of(createDefaultStep()));

            List<TaskLogVo> result = taskLogService.queryLogs(new TaskLog(), null, null);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTaskName()).isEqualTo("测试任务");
            assertThat(result.get(0).getStepName()).isEqualTo("测试步骤");
        }
    }

    @Test
    void testQueryLogs_TaskNotFound_ThrowsException() {
        try (var tenantMock = mockStatic(TenantContext.class);
             var securityMock = mockStatic(SecurityUtils.class)) {
            tenantMock.when(TenantContext::get).thenReturn(1L);
            securityMock.when(() -> SecurityUtils.isAdmin(anyLong())).thenReturn(false);

            TaskLog log = createDefaultLog();
            when(taskLogRepository.findTaskLogs(any(), any(), any(), any())).thenReturn(List.of(log));
            when(taskRepository.findById(TASK_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> taskLogService.queryLogs(new TaskLog(), null, null))
                    .isInstanceOf(TaskmgtException.class);
        }
    }

    // ==================== getLog ====================
    @Test
    void testGetLog_Success() {
        TaskLog log = createDefaultLog();
        when(taskLogRepository.findById(LOG_ID)).thenReturn(Optional.of(log));
        when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(createDefaultTask()));
        when(stepRepository.findById(STEP_ID)).thenReturn(Optional.of(createDefaultStep()));

        TaskLogVo result = taskLogService.getLog(LOG_ID);

        assertThat(result).isNotNull();
        assertThat(result.getTaskName()).isEqualTo("测试任务");
        assertThat(result.getStepName()).isEqualTo("测试步骤");
    }

    @Test
    void testGetLog_LogNotFound_ThrowsException() {
        when(taskLogRepository.findById(LOG_ID)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> taskLogService.getLog(LOG_ID))
                .isInstanceOf(TaskmgtException.class);
    }
}