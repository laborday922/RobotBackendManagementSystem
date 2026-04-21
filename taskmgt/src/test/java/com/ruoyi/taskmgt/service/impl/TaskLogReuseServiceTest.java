package com.ruoyi.taskmgt.service.impl;

import com.ruoyi.taskmgt.TestApplication;
import com.ruoyi.taskmgt.domain.TaskLogRepository;
import com.ruoyi.taskmgt.domain.bo.TaskLog;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskLogReuseServiceTest {

    @Mock
    private TaskLogRepository taskLogRepository;

    @InjectMocks
    private TaskLogReuseService taskLogReuseService;

    @Test
    void testRecord_Success() {
        Long taskId = 1L;
        Long stepId = 2L;
        String eventType = "TEST";
        String content = "日志内容";
        String operator = "admin";
        Long tenantId = 100L;

        taskLogReuseService.record(taskId, stepId, eventType, content, operator, tenantId);

        ArgumentCaptor<TaskLog> captor = ArgumentCaptor.forClass(TaskLog.class);
        verify(taskLogRepository, times(1)).insert(captor.capture());
        TaskLog log = captor.getValue();
        assertThat(log.getTaskId()).isEqualTo(taskId);
        assertThat(log.getStepId()).isEqualTo(stepId);
        assertThat(log.getEventType()).isEqualTo(eventType);
        assertThat(log.getContent()).isEqualTo(content);
        assertThat(log.getOperator()).isEqualTo(operator);
        assertThat(log.getTenantId()).isEqualTo(tenantId);
    }

    @Test
    void testRecord_Exception_ShouldNotPropagate() {
        doThrow(new RuntimeException("DB error")).when(taskLogRepository).insert(any(TaskLog.class));

        // 不应抛出异常
        taskLogReuseService.record(1L, null, "ERROR", "test", "system", null);

        verify(taskLogRepository).insert(any(TaskLog.class));
        // 没有异常抛出即成功
    }
}