package com.ruoyi.taskmgt.monitor;

import com.ruoyi.taskmgt.domain.StepRepository;
import com.ruoyi.taskmgt.domain.bo.TaskStep;
import com.ruoyi.taskmgt.event.AsyncTaskCompletedEvent;
import com.ruoyi.taskmgt.event.AsyncTaskTimeoutEvent;
import com.ruoyi.taskmgt.event.RobotCallbackEvent;
import com.ruoyi.taskmgt.operation.OperationHandler;
import com.ruoyi.taskmgt.operation.OperationRegistry;
import com.ruoyi.taskmgt.operation.model.OperationResult;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
@RequiredArgsConstructor
public class AsyncOperationMonitor {
    private final OperationRegistry operationRegistry;
    private final StepRepository stepRepository;
    private ScheduledExecutorService executor;
    private final Map<String, ScheduledFuture<?>> pollingTasks = new ConcurrentHashMap<>();
    private final Map<String, PollingContext> pollingContexts = new ConcurrentHashMap<>();
    private final ApplicationEventPublisher eventPublisher;
    // 记录已完成的异步任务（防止回调与轮询重复处理）
    private final Map<String, AtomicBoolean> completedTraceIds = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        executor = Executors.newScheduledThreadPool(4);
        log.info("异步操作监控器已启动");
    }

    @PreDestroy
    public void destroy() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
            log.info("异步操作监控器已关闭");
        }
    }

    public void registerPolling(Long stepId, String traceId, Long operationId, Date estimatedFinishTime) {
        OperationHandler handler = operationRegistry.getHandler(operationId);

        long initialDelay = 1000;
        long period = calculatePeriod(estimatedFinishTime);

        PollingContext context = new PollingContext();
        context.setStepId(stepId);
        context.setTraceId(traceId);
        context.setOperationId(operationId);
        context.setHandler(handler);
        context.setStartTime(new Date());
        context.setEstimatedFinishTime(estimatedFinishTime);
        context.setPollCount(0);

        pollingContexts.put(traceId, context);
        completedTraceIds.put(traceId, new AtomicBoolean(false));

        ScheduledFuture<?> future = executor.scheduleAtFixedRate(() -> {
            try {
                doPolling(traceId);
            } catch (Exception e) {
                log.error("轮询监控异常, traceId={}", traceId, e);
            }
        }, initialDelay, period, TimeUnit.MILLISECONDS);

        pollingTasks.put(traceId, future);
        log.info("已注册轮询监控: stepId={}, traceId={}, 轮询间隔={}ms", stepId, traceId, period);
    }

    private void doPolling(String traceId) {
        AtomicBoolean completedFlag = completedTraceIds.get(traceId);
        if (completedFlag != null && completedFlag.get()) {
            cancelPolling(traceId);
            return;
        }

        PollingContext context = pollingContexts.get(traceId);
        if (context == null) return;

        context.setPollCount(context.getPollCount() + 1);

        try {
            OperationResult status = queryStatus(context.getHandler(), traceId, context.getRobotId());
            if (status == null) {
                log.warn("轮询返回空状态, traceId={}", traceId);
                return;
            }

            log.debug("轮询状态: traceId={}, 完成={}, 进度={}", traceId, status.isCompleted(), status.getProgress());

            if (status.isCompleted()) {
                if (completedFlag != null && completedFlag.compareAndSet(false, true)) {
                    log.info("异步任务已完成, traceId={}", traceId);
                    eventPublisher.publishEvent(new AsyncTaskCompletedEvent(this, context.getStepId(), status));
                    cancelPolling(traceId);
                }
            } else if (isTimeout(context)) {
                if (completedFlag != null && completedFlag.compareAndSet(false, true)) {
                    log.warn("异步任务超时, traceId={}, 已等待{}秒", traceId, getElapsedSeconds(context));
                    eventPublisher.publishEvent(new AsyncTaskTimeoutEvent(this, context.getStepId()));
                    cancelPolling(traceId);
                }
            }
        } catch (Exception e) {
            log.error("轮询查询失败, traceId={}", traceId, e);
            if (context.getPollCount() > 30) {
                if (completedFlag != null && completedFlag.compareAndSet(false, true)) {
                    log.error("轮询次数过多，放弃监控, traceId={}", traceId);
                    eventPublisher.publishEvent(new AsyncTaskTimeoutEvent(this, context.getStepId()));
                    cancelPolling(traceId);
                }
            }
        }
    }

    public void handleRobotCallback(String traceId, RobotCallbackData callbackData) {
        log.info("收到机器人回调, traceId={}, success={}", traceId, callbackData.isSuccess());
        AtomicBoolean completedFlag = completedTraceIds.computeIfAbsent(traceId, k -> new AtomicBoolean(false));
        if (!completedFlag.compareAndSet(false, true)) {
            log.warn("重复处理回调, traceId={}", traceId);
            return;
        }
        TaskStep step = stepRepository.findByTraceId(traceId);
        if (step == null) {
            log.error("回调traceId不存在: {}", traceId);
            return;
        }
        eventPublisher.publishEvent(new RobotCallbackEvent(this, traceId, callbackData));
        cancelPolling(traceId);

    }

    public void cancelPolling(String traceId) {
        ScheduledFuture<?> future = pollingTasks.remove(traceId);
        if (future != null) {
            future.cancel(false);
            log.debug("已取消轮询: {}", traceId);
        }
        pollingContexts.remove(traceId);
    }

    /**
     * 查询任务状态（通过handler）
     */
    private OperationResult queryStatus(OperationHandler handler, String traceId, Long robotId) {
        // 创建查询用的虚拟step
        TaskStep queryStep = new TaskStep();
        queryStep.setTraceId(traceId);

        // 构造查询参数
        Map<String, Object> queryParams = Map.of(
                "traceId", traceId,
                "action", "query"
        );

        // 执行查询（复用handler的execute，通过特殊参数标识为查询）
        OperationResult result = handler.execute(queryStep, robotId, queryParams);

        // 转换结果为状态查询结果
        return OperationResult.builder()
                .success(result.isSuccess())
                .completed(isTaskCompleted(result))
                .progress(getProgressFromResult(result))
                .message(result.getMessage())
                .data(result.getData())
                .build();
    }

    private long calculatePeriod(Date estimatedFinishTime) {
        if (estimatedFinishTime == null) {
            return 2000; // 默认2秒
        }

        long remaining = estimatedFinishTime.getTime() - System.currentTimeMillis();

        if (remaining < 5000) {
            return 500; // 剩余5秒内，每500ms查一次
        } else if (remaining < 30000) {
            return 2000; // 剩余30秒内，每2秒查一次
        } else {
            return 5000; // 否则每5秒查一次
        }
    }

    private boolean isTimeout(PollingContext context) {
        // 超过预计完成时间30%或绝对超时1小时
        Date estimated = context.getEstimatedFinishTime();
        if (estimated == null) return false;

        long elapsed = System.currentTimeMillis() - context.getStartTime().getTime();
        long expected = estimated.getTime() - context.getStartTime().getTime();

        return elapsed > expected * 1.3 || elapsed > 3600000; // 1小时绝对超时
    }

    private long getElapsedSeconds(PollingContext context) {
        return (System.currentTimeMillis() - context.getStartTime().getTime()) / 1000;
    }

    private boolean isTaskCompleted(OperationResult result) {
        // 从结果判断是否完成
        if (result.getData() instanceof Map) {
            Map<?, ?> data = (Map<?, ?>) result.getData();
            Object status = data.get("status");
            return "COMPLETED".equals(status) || "FINISHED".equals(status) || "SUCCESS".equals(status);
        }
        return result.isSuccess(); // 简化判断
    }

    private int getProgressFromResult(OperationResult result) {
        if (result.getData() instanceof Map) {
            Map<?, ?> data = (Map<?, ?>) result.getData();
            Object progress = data.get("progress");
            if (progress instanceof Number) {
                return ((Number) progress).intValue();
            }
        }
        return result.isSuccess() ? 100 : 0;
    }

    @Data
    public static class PollingContext {
        private Long stepId;
        private String traceId;
        private Long operationId;
        private Long robotId;
        private OperationHandler handler;
        private Date startTime;
        private Date estimatedFinishTime;
        private int pollCount;
    }

    @Data
    public static class RobotCallbackData {
        private boolean success;
        private String errorMsg;
        private Object data;
        private String traceId;
        private Long robotId;
        private Date timestamp;
    }
}