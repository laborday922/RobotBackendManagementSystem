package com.ruoyi.taskmgt.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.enums.ReturnNo;
import com.ruoyi.common.exception.task.TaskmgtException;
import com.ruoyi.robots.domain.Robot;
import com.ruoyi.robots.service.IRobotsService;
import com.ruoyi.taskmgt.TestApplication;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class StepExecutionServiceTest {

    @Mock private StepRepository stepRepository;
    @Mock private TaskLogReuseService taskLogService;
    @Mock private IRobotsService robotService;
    @Mock private ObjectMapper objectMapper;
    @Mock private RobotLoadBalancer robotLoadBalancer;
    @Mock private MessageSourceAccessor messageSourceAccessor;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private AsyncOperationMonitor asyncMonitor;
    @Mock private RobotInvoker robotInvoker;
    @Mock private RedisCache redisCache;
    @InjectMocks
    private StepExecutionService stepExecutionService;

    private TaskStep step;
    private Task task;
    private final Long STEP_ID = 1L;
    private final Long TASK_ID = 10L;
    private final Long ROBOT_ID = 100L;
    private final Long API_ID = 200L;

    @BeforeEach
    void setUp() {
        step = new TaskStep();
        step.setId(STEP_ID);
        step.setTaskId(TASK_ID);
        step.setStatus(TaskStep.NOTSTART);
        step.setStepName("测试步骤");
        step.setOperationId(API_ID);
        step.setOperationJson("{\"param\":\"value\"}");

        task = new Task();
        task.setId(TASK_ID);
        task.setIsGroupTask(0);
        task.setRobotId(ROBOT_ID);
        when(redisCache.deleteObject(anyList())).thenReturn(true);
    }

    @Test
    void testExecuteStep_SyncSuccess() throws Exception {
        when(stepRepository.findById(STEP_ID)).thenReturn(Optional.of(step));
        when(robotInvoker.isRobotOnline(ROBOT_ID)).thenReturn(true);
        when(stepRepository.update(any(TaskStep.class))).thenReturn(Collections.emptyList());

        RobotTaskResponse response = new RobotTaskResponse();
        response.setMode("SYNC");
        response.setSuccess(true);
        response.setData("result");
        when(robotInvoker.execute(eq(ROBOT_ID), any(RobotTaskRequest.class))).thenReturn(response);
        when(objectMapper.readValue(anyString(), (TypeReference<Object>) any())).thenReturn(new HashMap<>());

        stepExecutionService.executeStep(step, task);

        // 验证两次更新：开始执行 + 完成
        verify(stepRepository, times(2)).update(step);
        assertThat(step.getStatus()).isEqualTo(TaskStep.FINISHED);
        // 验证两次事件发布：completeStep 内一次 + executeStep 内一次
        verify(eventPublisher, times(2)).publishEvent(any(StepCompletedEvent.class));
        verify(taskLogService, atLeastOnce()).record(any(), any(), any(), any(), any(), any());
    }

    @Test
    void testExecuteStep_SyncFailure() throws Exception {
        when(stepRepository.findById(STEP_ID)).thenReturn(Optional.of(step));
        when(robotInvoker.isRobotOnline(ROBOT_ID)).thenReturn(true);
        when(stepRepository.update(any(TaskStep.class))).thenReturn(Collections.emptyList());

        RobotTaskResponse response = new RobotTaskResponse();
        response.setMode("SYNC");
        response.setSuccess(false);
        response.setErrorMsg("执行失败");
        when(robotInvoker.execute(eq(ROBOT_ID), any(RobotTaskRequest.class))).thenReturn(response);
        when(objectMapper.readValue(anyString(), (TypeReference<Object>) any())).thenReturn(new HashMap<>());

        stepExecutionService.executeStep(step, task);

        verify(stepRepository, times(2)).update(step);
        assertThat(step.getStatus()).isEqualTo(TaskStep.PAUSED);
        assertThat(step.getErrorMsg()).isEqualTo("执行失败");
        // 同步失败只发布一次事件（在 executeStep 中）
        verify(eventPublisher, times(1)).publishEvent(any(StepCompletedEvent.class));
    }
    @Test
    void testExecuteStep_AsyncMode() throws Exception {
        when(stepRepository.findById(STEP_ID)).thenReturn(Optional.of(step));
        when(robotInvoker.isRobotOnline(ROBOT_ID)).thenReturn(true);
        when(stepRepository.update(any(TaskStep.class))).thenReturn(Collections.emptyList());

        RobotTaskResponse response = new RobotTaskResponse();
        response.setMode("ASYNC");
        response.setTraceId("trace-123");
        response.setEstimatedFinishTime(new Date(System.currentTimeMillis() + 60000));
        when(robotInvoker.execute(eq(ROBOT_ID), any(RobotTaskRequest.class))).thenReturn(response);
        when(objectMapper.readValue(anyString(), (TypeReference<Object>) any())).thenReturn(new HashMap<>());

        stepExecutionService.executeStep(step, task);

        // 两次更新：开始执行 -> EXECUTING，然后 -> WAITING
        verify(stepRepository, times(2)).update(step);
        assertThat(step.getStatus()).isEqualTo(TaskStep.WAITING);
        assertThat(step.getTraceId()).isEqualTo("trace-123");
        verify(asyncMonitor).registerPolling(eq(STEP_ID), eq("trace-123"), eq(API_ID), eq(ROBOT_ID), any());
        // 异步模式不发布完成事件
        verify(eventPublisher, never()).publishEvent(any(StepCompletedEvent.class));
    }

    @Test
    void testExecuteStep_CallbackMode() throws Exception {
        when(stepRepository.findById(STEP_ID)).thenReturn(Optional.of(step));
        when(robotInvoker.isRobotOnline(ROBOT_ID)).thenReturn(true);
        when(stepRepository.update(any(TaskStep.class))).thenReturn(Collections.emptyList());

        RobotTaskResponse response = new RobotTaskResponse();
        response.setMode("CALLBACK");
        response.setTraceId("trace-456");
        when(robotInvoker.execute(eq(ROBOT_ID), any(RobotTaskRequest.class))).thenReturn(response);
        when(objectMapper.readValue(anyString(), (TypeReference<Object>) any())).thenReturn(new HashMap<>());

        stepExecutionService.executeStep(step, task);

        verify(stepRepository, times(2)).update(step);
        assertThat(step.getStatus()).isEqualTo(TaskStep.WAITING_CALLBACK);
        verify(asyncMonitor).registerCallbackTimeout(eq(STEP_ID), eq("trace-456"), anyLong());
    }

    @Test
    void testCompleteStep_Success() {
        step.setStatus(TaskStep.EXECUTING);
        when(stepRepository.findById(STEP_ID)).thenReturn(Optional.of(step));
        when(stepRepository.update(step)).thenReturn(Collections.emptyList());

        stepExecutionService.completeStep(STEP_ID, "success");

        assertThat(step.getStatus()).isEqualTo(TaskStep.FINISHED);
        assertThat(step.getEndTime()).isNotNull();
        verify(taskLogService).record(eq(TASK_ID), eq(STEP_ID), eq(TaskLogEventType.STEP_COMPLETE), any(), any(), any());
        verify(eventPublisher).publishEvent(any(StepCompletedEvent.class));
    }

    @Test
    void testCompleteStep_InvalidStatus() {
        step.setStatus(TaskStep.PAUSED);
        when(stepRepository.findById(STEP_ID)).thenReturn(Optional.of(step));

        stepExecutionService.completeStep(STEP_ID, "data");

        verify(stepRepository, never()).update(step);
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void testFailStep_Success() {
        step.setStatus(TaskStep.EXECUTING);
        when(stepRepository.findById(STEP_ID)).thenReturn(Optional.of(step));
        when(stepRepository.update(step)).thenReturn(Collections.emptyList());

        stepExecutionService.failStep(STEP_ID, "error message");

        assertThat(step.getStatus()).isEqualTo(TaskStep.PAUSED);
        assertThat(step.getErrorMsg()).isEqualTo("error message");
        verify(taskLogService).record(eq(TASK_ID), eq(STEP_ID), eq(TaskLogEventType.STEP_FAILED), contains("error message"), any(), any());
    }

    @Test
    void testResolveRobotId_GroupTaskWithLoadBalancer() {
        task.setIsGroupTask(1);
        task.setRobotGroupId(50L);
        step.setAssignedRobotId(null);

        List<Robot> robots = new ArrayList<>();
        Robot r1 = new Robot();
        r1.setId(101L);
        Robot r2 = new Robot();
        r2.setId(102L);
        robots.add(r1);
        robots.add(r2);

        when(robotService.selectRobotsList(any(Robot.class))).thenReturn(robots);
        when(robotLoadBalancer.selectRobot(anyList(), eq(step))).thenReturn(102L);
        when(stepRepository.update(step)).thenReturn(Collections.emptyList());

        Long result = stepExecutionService.resolveRobotId(task, step);
        assertThat(result).isEqualTo(102L);
        assertThat(step.getAssignedRobotId()).isEqualTo(102L);
    }

    @Test
    void testResolveRobotId_GroupTaskNoAvailableRobot() {
        task.setIsGroupTask(1);
        task.setRobotGroupId(50L);
        when(robotService.selectRobotsList(any(Robot.class))).thenReturn(Collections.emptyList());
        when(messageSourceAccessor.getMessage(anyString())).thenReturn("机器人组内无任务可用机器人");

        assertThatThrownBy(() -> stepExecutionService.resolveRobotId(task, step))
                .isInstanceOf(TaskmgtException.class);
    }
}