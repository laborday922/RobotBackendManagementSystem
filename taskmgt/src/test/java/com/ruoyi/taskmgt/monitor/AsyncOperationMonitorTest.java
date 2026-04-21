package com.ruoyi.taskmgt.monitor;

import com.ruoyi.robots.event.WebSocketAsyncResultEvent;
import com.ruoyi.taskmgt.domain.StepRepository;
import com.ruoyi.taskmgt.domain.bo.TaskStep;
import com.ruoyi.taskmgt.event.AsyncTaskCompletedEvent;
import com.ruoyi.taskmgt.event.AsyncTaskTimeoutEvent;
import com.ruoyi.taskmgt.event.RobotCallbackEvent;
import com.ruoyi.taskmgt.invoker.RobotInvoker;
import com.ruoyi.taskmgt.invoker.dto.TaskStatusResponse;
import com.ruoyi.taskmgt.monitor.dto.OperationResult;
import com.ruoyi.taskmgt.monitor.dto.RobotCallbackData;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Duration;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
public class AsyncOperationMonitorTest {

    @Mock private StepRepository stepRepository;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private RobotInvoker robotInvoker;

    @InjectMocks
    private AsyncOperationMonitor monitor;

    @BeforeEach
    void setUp() {
        monitor.init();
    }

    @AfterEach
    void tearDown() {
        monitor.destroy();
    }

    // ==================== 只测试同步方法 ====================

    @Test
    void testHandleRobotCallback_Success() {
        TaskStep step = new TaskStep();
        step.setId(1L);
        when(stepRepository.findByTraceId("trace-123")).thenReturn(step);

        RobotCallbackData data = new RobotCallbackData();
        data.setTraceId("trace-123");
        data.setSuccess(true);

        monitor.handleRobotCallback("trace-123", data);

        verify(eventPublisher).publishEvent(any(RobotCallbackEvent.class));
    }

    @Test
    void testHandleRobotCallback_StepNotFound() {
        when(stepRepository.findByTraceId("trace-123")).thenReturn(null);

        RobotCallbackData data = new RobotCallbackData();
        data.setTraceId("trace-123");
        data.setSuccess(true);

        monitor.handleRobotCallback("trace-123", data);

        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void testHandleRobotCallback_Duplicate() {
        TaskStep step = new TaskStep();
        step.setId(1L);
        when(stepRepository.findByTraceId("trace-123")).thenReturn(step);

        RobotCallbackData data = new RobotCallbackData();
        data.setTraceId("trace-123");
        data.setSuccess(true);

        monitor.handleRobotCallback("trace-123", data);
        monitor.handleRobotCallback("trace-123", data); // 重复

        verify(eventPublisher, times(1)).publishEvent(any());
    }

    @Test
    void testHandleWebSocketCallback_Success() {
        TaskStep step = new TaskStep();
        step.setId(1L);
        when(stepRepository.findByTraceId("trace-123")).thenReturn(step);

        monitor.handleWebSocketCallback("trace-123", true, "data", null);

        ArgumentCaptor<AsyncTaskCompletedEvent> captor = ArgumentCaptor.forClass(AsyncTaskCompletedEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().getResult().isSuccess()).isTrue();
    }

    @Test
    void testHandleWebSocketCallback_Failure() {
        TaskStep step = new TaskStep();
        step.setId(1L);
        when(stepRepository.findByTraceId("trace-123")).thenReturn(step);

        monitor.handleWebSocketCallback("trace-123", false, null, "error");

        ArgumentCaptor<AsyncTaskCompletedEvent> captor = ArgumentCaptor.forClass(AsyncTaskCompletedEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().getResult().isSuccess()).isFalse();
        assertThat(captor.getValue().getResult().getMessage()).isEqualTo("error");
    }

    @Test
    void testHandleWebSocketCallback_StepNotFound() {
        when(stepRepository.findByTraceId("trace-123")).thenReturn(null);

        monitor.handleWebSocketCallback("trace-123", true, null, null);

        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void testHandleWebSocketCallback_Duplicate() {
        TaskStep step = new TaskStep();
        step.setId(1L);
        when(stepRepository.findByTraceId("trace-123")).thenReturn(step);

        monitor.handleWebSocketCallback("trace-123", true, null, null);
        monitor.handleWebSocketCallback("trace-123", true, null, null);

        verify(eventPublisher, times(1)).publishEvent(any());
    }

    @Test
    void testOnWebSocketAsyncResult() {
        TaskStep step = new TaskStep();
        step.setId(1L);
        when(stepRepository.findByTraceId("trace-123")).thenReturn(step);

        WebSocketAsyncResultEvent event = new WebSocketAsyncResultEvent(this, "trace-123", true, "data", null);
        monitor.onWebSocketAsyncResult(event);

        verify(eventPublisher).publishEvent(any(AsyncTaskCompletedEvent.class));
    }

    @Test
    void testCancelPolling_NotExists() {
        // 不应抛异常
        monitor.cancelPolling("non-existent");
    }

    @Test
    void testRegisterPolling() {
        // 只验证能正常注册
        monitor.registerPolling(1L, "trace-123", 100L, 200L, new Date(System.currentTimeMillis() + 60000));
        // 验证不抛异常即可
    }

    @Test
    void testRegisterCallbackTimeout_AlreadyCompleted() throws InterruptedException {
        // 先完成
        TaskStep step = new TaskStep();
        step.setId(1L);
        when(stepRepository.findByTraceId("trace-123")).thenReturn(step);
        monitor.handleWebSocketCallback("trace-123", true, null, null);

        // 再注册超时
        monitor.registerCallbackTimeout(1L, "trace-123", 50);
        Thread.sleep(100);

        // 不应发布超时事件
        verify(eventPublisher, times(1)).publishEvent(any()); // 只有 WebSocket 回调的事件
    }
}