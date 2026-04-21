package com.ruoyi.taskmgt.service.impl;

import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.taskmgt.TestApplication;
import com.ruoyi.taskmgt.constants.TaskLogEventType;
import com.ruoyi.taskmgt.domain.StepRepository;
import com.ruoyi.taskmgt.domain.TaskRepository;
import com.ruoyi.taskmgt.domain.bo.Task;
import com.ruoyi.taskmgt.domain.bo.TaskStep;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskReuseServiceTest {

    @Mock private StepRepository stepRepository;
    @Mock private TaskLogReuseService taskLogService;
    @Mock private TaskRepository taskRepository;
    @Mock private RedisCache redisUtil;

    @InjectMocks
    private TaskReuseService taskReuseService;

    private Task task;
    private final Long TASK_ID = 1L;

    @BeforeEach
    void setUp() {
        task = new Task();
        task.setId(TASK_ID);
        task.setStatus(Task.EXECUTING);
        task.setIsGroupTask(0);
    }

    @Test
    void testCompleteTask_PeriodicTask_WithCron() {
        task.setTaskType(1);
        task.setCronExpression("0 0 9 * * ?");
        task.setScheduledTime(new Date());

        List<TaskStep> steps = new ArrayList<>();
        TaskStep step1 = new TaskStep();
        step1.setId(1L);
        step1.setStatus(TaskStep.FINISHED);
        steps.add(step1);
        when(stepRepository.findStepsByTaskId(TASK_ID)).thenReturn(steps);
        when(stepRepository.update(any(TaskStep.class))).thenReturn(Collections.emptyList());
        when(taskRepository.update(any(Task.class))).thenReturn(Collections.emptyList());

        taskReuseService.completeTask(task);

        assertThat(task.getStatus()).isEqualTo(Task.NOTSTART);
        assertThat(task.getScheduledTime()).isNotNull();
        assertThat(task.getRiskLevel()).isZero();
        verify(taskLogService).record(eq(TASK_ID), isNull(), eq(TaskLogEventType.TASK_COMPLETE), contains("下次执行时间"), any(), any());
        verify(stepRepository, atLeastOnce()).update(any(TaskStep.class));
    }

    @Test
    void testCompleteTask_OneTimeTask() {
        task.setTaskType(0); // 一次性任务
        task.setCronExpression(null);

        when(taskRepository.update(any(Task.class))).thenReturn(Collections.emptyList());

        taskReuseService.completeTask(task);

        assertThat(task.getStatus()).isEqualTo(Task.FINISHED);
        verify(taskLogService).record(eq(TASK_ID), isNull(), eq(TaskLogEventType.TASK_COMPLETE), anyString(), any(), any());
        verify(taskRepository).update(task);
    }

    @Test
    void testCompleteTask_PeriodicTask_NoCron() {
        task.setTaskType(1);
        task.setCronExpression(null);
        task.setScheduledTime(null);

        when(taskRepository.update(any(Task.class))).thenReturn(Collections.emptyList());

        taskReuseService.completeTask(task);

        // 没有 cron 表达式，视为一次性任务
        assertThat(task.getStatus()).isEqualTo(Task.FINISHED);
    }

    @Test
    void testCompleteTask_InvalidStatusTransition() {
        task.setStatus(Task.FINISHED); // 已经完成，不允许再转换
        taskReuseService.completeTask(task);
        verify(taskRepository, never()).update(any());
        verify(taskLogService, never()).record(any(), any(), any(), any(), any(), any());
    }

    @Test
    void testCalculateNextScheduledTime_ValidCron() {
        Task t = new Task();
        t.setCronExpression("0 0 12 * * ?");
        t.setScheduledTime(new Date());
    }
}