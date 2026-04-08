package com.ruoyi.taskmgt.listener;

import com.ruoyi.taskmgt.domain.StepRepository;
import com.ruoyi.taskmgt.domain.bo.TaskStep;
import com.ruoyi.taskmgt.event.AsyncTaskCompletedEvent;
import com.ruoyi.taskmgt.event.AsyncTaskTimeoutEvent;
import com.ruoyi.taskmgt.event.RobotCallbackEvent;
import com.ruoyi.taskmgt.event.StepCompletedEvent;
import com.ruoyi.taskmgt.monitor.dto.RobotCallbackData;
import com.ruoyi.taskmgt.operation.dto.OperationResult;
import com.ruoyi.taskmgt.service.impl.StepExecutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class AsyncStepListener {

    private final StepExecutionService stepExecutionService;
    private final StepRepository stepRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final ConcurrentHashMap<String, Boolean> asyncProcessingMap = new ConcurrentHashMap<>();

    @EventListener
    @Transactional
    public void onAsyncStepCompleted(AsyncTaskCompletedEvent event) {
        Long stepId = event.getStepId();
        String key = "async_" + stepId;
        if (asyncProcessingMap.putIfAbsent(key, Boolean.TRUE) != null) {
            log.warn("重复处理异步完成, stepId={}", stepId);
            return;
        }
        try {
            OperationResult result = event.getResult();
            if (result.isSuccess()) {
                stepExecutionService.completeStep(stepId, result.getData());
                // 触发下一步
                TaskStep step = stepRepository.findById(stepId).orElse(null);
                if (step != null) {
                    eventPublisher.publishEvent(new StepCompletedEvent(this, step.getTaskId(), stepId, true));
                }
            } else {
                stepExecutionService.failStep(stepId, result.getMessage());
            }
        } finally {
            asyncProcessingMap.remove(key);
        }
    }

    @EventListener
    @Transactional
    public void onAsyncStepTimeout(AsyncTaskTimeoutEvent event) {
        Long stepId = event.getStepId();
        String key = "timeout_" + stepId;
        if (asyncProcessingMap.putIfAbsent(key, Boolean.TRUE) != null) {
            log.warn("重复处理异步超时, stepId={}", stepId);
            return;
        }
        try {
            stepExecutionService.failStep(stepId, "异步任务执行超时");
        } finally {
            asyncProcessingMap.remove(key);
        }
    }

    @EventListener
    @Transactional
    public void onRobotCallback(RobotCallbackEvent event) {
        String traceId = event.getTraceId();
        RobotCallbackData callbackData = event.getCallbackData();

        log.info("处理机器人回调, traceId={}, success={}", traceId, callbackData.isSuccess());

        // 防重复处理逻辑
        String key = "callback_" + traceId;
        if (asyncProcessingMap.putIfAbsent(key, Boolean.TRUE) != null) {
            log.warn("重复处理回调, traceId={}", traceId);
            return;
        }
        try {
            TaskStep step = stepRepository.findByTraceId(traceId);
            if (step == null) {
                log.error("回调traceId不存在: {}", traceId);
                return;
            }

            if (callbackData.isSuccess()) {
                stepExecutionService.completeStep(step.getId(), callbackData.getData());
                // 触发下一步
                eventPublisher.publishEvent(new StepCompletedEvent(this, step.getTaskId(), step.getId(), true));
            } else {
                stepExecutionService.failStep(step.getId(), callbackData.getErrorMsg());
            }
        } finally {
            asyncProcessingMap.remove(key);
        }
    }
}