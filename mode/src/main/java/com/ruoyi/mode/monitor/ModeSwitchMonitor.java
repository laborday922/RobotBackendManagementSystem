package com.ruoyi.mode.monitor;

import com.ruoyi.mode.event.ModeSwitchCompletedEvent;
import com.ruoyi.mode.event.ModeSwitchTimeoutEvent;
import com.ruoyi.mode.invoker.ModeSwitchInvoker;
import com.ruoyi.mode.invoker.dto.ModeSwitchResponse;
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

/**
 * 模式切换异步监控器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ModeSwitchMonitor {

    private final ModeSwitchInvoker modeSwitchInvoker;
    private final ApplicationEventPublisher eventPublisher;

    private ScheduledExecutorService executor;
    private final Map<String, ScheduledFuture<?>> pollingTasks = new ConcurrentHashMap<>();
    private final Map<String, SwitchContext> switchContexts = new ConcurrentHashMap<>();
    private final Map<String, AtomicBoolean> completedTraceIds = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        executor = Executors.newScheduledThreadPool(2);
        log.info("模式切换监控器已启动");
    }

    @PreDestroy
    public void destroy() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
            log.info("模式切换监控器已关闭");
        }
    }

    /**
     * 注册轮询监控
     */
    public void registerPolling(Long robotId, String traceId, Date estimatedFinishTime) {
        long initialDelay = 1000;
        long period = calculatePeriod(estimatedFinishTime);

        SwitchContext context = new SwitchContext();
        context.setRobotId(robotId);
        context.setTraceId(traceId);
        context.setStartTime(new Date());
        context.setEstimatedFinishTime(estimatedFinishTime);
        context.setPollCount(0);

        switchContexts.put(traceId, context);
        completedTraceIds.put(traceId, new AtomicBoolean(false));

        ScheduledFuture<?> future = executor.scheduleAtFixedRate(() -> {
            try {
                doPolling(traceId);
            } catch (Exception e) {
                log.error("轮询监控异常, traceId={}", traceId, e);
            }
        }, initialDelay, period, TimeUnit.MILLISECONDS);

        pollingTasks.put(traceId, future);
        log.info("已注册模式切换轮询监控: robotId={}, traceId={}", robotId, traceId);
    }

    private void doPolling(String traceId) {
        AtomicBoolean completedFlag = completedTraceIds.get(traceId);
        if (completedFlag != null && completedFlag.get()) {
            cancelPolling(traceId);
            return;
        }

        SwitchContext context = switchContexts.get(traceId);
        if (context == null) return;

        context.setPollCount(context.getPollCount() + 1);

        try {
            ModeSwitchResponse status = modeSwitchInvoker.querySwitchStatus(context.getRobotId(), traceId);
            if (status == null) return;

            if (status.isCompleted()) {
                if (completedFlag != null && completedFlag.compareAndSet(false, true)) {
                    log.info("模式切换已完成, traceId={}, success={}", traceId, status.isSuccess());
                    eventPublisher.publishEvent(new ModeSwitchCompletedEvent(this, context.getRobotId(), traceId, status.isSuccess(), status.getData(), status.getErrorMsg()));
                    cancelPolling(traceId);
                }
            } else if (isTimeout(context)) {
                if (completedFlag != null && completedFlag.compareAndSet(false, true)) {
                    log.warn("模式切换超时, traceId={}", traceId);
                    eventPublisher.publishEvent(new ModeSwitchTimeoutEvent(this, context.getRobotId(), traceId));
                    cancelPolling(traceId);
                }
            }
        } catch (Exception e) {
            log.error("轮询查询失败, traceId={}", traceId, e);
            if (context.getPollCount() > 30) {
                if (completedFlag != null && completedFlag.compareAndSet(false, true)) {
                    log.error("轮询次数过多，放弃监控, traceId={}", traceId);
                    eventPublisher.publishEvent(new ModeSwitchTimeoutEvent(this, context.getRobotId(), traceId));
                    cancelPolling(traceId);
                }
            }
        }
    }

    private long calculatePeriod(Date estimatedFinishTime) {
        if (estimatedFinishTime == null) return 2000;
        long remaining = estimatedFinishTime.getTime() - System.currentTimeMillis();
        if (remaining < 5000) return 500;
        if (remaining < 30000) return 2000;
        return 5000;
    }

    private boolean isTimeout(SwitchContext context) {
        Date estimated = context.getEstimatedFinishTime();
        if (estimated == null) return false;
        long elapsed = System.currentTimeMillis() - context.getStartTime().getTime();
        long expected = estimated.getTime() - context.getStartTime().getTime();
        return elapsed > expected * 1.3 || elapsed > 300000; // 5分钟最大超时
    }

    public void cancelPolling(String traceId) {
        ScheduledFuture<?> future = pollingTasks.remove(traceId);
        if (future != null) future.cancel(false);
        switchContexts.remove(traceId);
        completedTraceIds.remove(traceId);
    }

    @lombok.Data
    public static class SwitchContext {
        private Long robotId;
        private String traceId;
        private Date startTime;
        private Date estimatedFinishTime;
        private int pollCount;
    }
}