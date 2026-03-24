package com.ruoyi.taskmgt.service.operation;

import com.ruoyi.taskmgt.domain.StepRepository;
import com.ruoyi.taskmgt.domain.bo.TaskStep;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.*;

@Slf4j
@Component
public class AsyncOperationMonitor {

    @Autowired
    private OperationRegistry operationRegistry;
    @Autowired
    private StepRepository stepRepository;
    @Autowired
    private StepExecutionEngine stepExecutionEngine;

    private ScheduledExecutorService executor;
    private final Map<String, ScheduledFuture<?>> pollingTasks = new ConcurrentHashMap<>();
    private final Map<String, PollingContext> pollingContexts = new ConcurrentHashMap<>();

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

    /**
     * 注册轮询监控（用于ASYNC类型）
     */
    public void registerPolling(Long stepId, String traceId, Long operationId, Date estimatedFinishTime) {
        OperationHandler handler = operationRegistry.getHandler(operationId);

        // 计算轮询间隔
        long initialDelay = 1000;  // 1秒后开始
        long period = calculatePeriod(estimatedFinishTime);

        // 保存上下文
        PollingContext context = new PollingContext();
        context.setStepId(stepId);
        context.setTraceId(traceId);
        context.setOperationId(operationId);
        context.setHandler(handler);
        context.setStartTime(new Date());
        context.setEstimatedFinishTime(estimatedFinishTime);
        context.setPollCount(0);

        pollingContexts.put(traceId, context);

        ScheduledFuture<?> future = executor.scheduleAtFixedRate(() -> {
            try {
                doPolling(traceId);
            } catch (Exception e) {
                log.error("轮询监控异常, traceId={}", traceId, e);
            }
        }, initialDelay, period, TimeUnit.MILLISECONDS);

        pollingTasks.put(traceId, future);

        log.info("已注册轮询监控: stepId={}, traceId={}, 轮询间隔={}ms",
                stepId, traceId, period);
    }

    /**
     * 执行轮询
     */
    private void doPolling(String traceId) {
        PollingContext context = pollingContexts.get(traceId);
        if (context == null) return;

        context.setPollCount(context.getPollCount() + 1);

        try {
            // 查询任务状态
            OperationResult status = queryStatus(context.getHandler(), traceId, context.getRobotId());

            if (status == null) {
                log.warn("轮询返回空状态, traceId={}", traceId);
                return;
            }

            log.debug("轮询状态: traceId={}, 完成={}, 进度={}",
                    traceId, status.isCompleted(), status.getProgress());

            if (status.isCompleted()) {
                // 任务完成
                log.info("异步任务已完成, traceId={}", traceId);
                onAsyncComplete(context.getStepId(), status);
                cancelPolling(traceId);

            } else if (isTimeout(context)) {
                // 超时处理
                log.warn("异步任务超时, traceId={}, 已等待{}秒",
                        traceId, getElapsedSeconds(context));
                stepExecutionEngine.onAsyncTimeout(context.getStepId());
                cancelPolling(traceId);

            } else {
                // 更新进度（可选）
                if (status.getProgress() > 0) {
                    updateStepProgress(context.getStepId(), status.getProgress());
                }
            }

        } catch (Exception e) {
            log.error("轮询查询失败, traceId={}", traceId, e);
            // 连续失败多次后可考虑取消
            if (context.getPollCount() > 30) { // 30次后放弃
                log.error("轮询次数过多，放弃监控, traceId={}", traceId);
                stepExecutionEngine.onAsyncTimeout(context.getStepId());
                cancelPolling(traceId);
            }
        }
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

    /**
     * 处理机器人回调（用于CALLBACK类型）
     */
    public void handleRobotCallback(String traceId, RobotCallbackData callbackData) {
        log.info("收到机器人回调, traceId={}, success={}", traceId, callbackData.isSuccess());

        // 查找步骤
        TaskStep step = stepRepository.findByTraceId(traceId);
        if (step == null) {
            log.error("回调traceId不存在: {}", traceId);
            return;
        }

        // 根据回调结果处理
        if (callbackData.isSuccess()) {
            stepExecutionEngine.completeStep(step, callbackData.getData());
            // 触发下一步
            stepExecutionEngine.onAsyncComplete(step.getId(),
                    OperationResult.builder().success(true).data(callbackData.getData()).build());
        } else {
            stepExecutionEngine.failStep(step, callbackData.getErrorMsg());
        }
    }

    /**
     * 取消轮询
     */
    public void cancelPolling(String traceId) {
        ScheduledFuture<?> future = pollingTasks.remove(traceId);
        if (future != null) {
            future.cancel(false);
            log.debug("已取消轮询: {}", traceId);
        }
        pollingContexts.remove(traceId);
    }

    // ========== 辅助方法 ==========

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

    private void updateStepProgress(Long stepId, int progress) {
        // 可选：更新步骤进度到数据库或Redis，供前端查询
        // stepRepository.updateProgress(stepId, progress);
    }

    private void onAsyncComplete(Long stepId, OperationResult result) {
        stepExecutionEngine.onAsyncComplete(stepId, result);
    }

    // ========== 内部类 ==========

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