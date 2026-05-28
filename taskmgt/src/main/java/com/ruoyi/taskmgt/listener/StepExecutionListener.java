package com.ruoyi.taskmgt.listener;

import com.ruoyi.taskmgt.event.ExecuteStepEvent;
import com.ruoyi.taskmgt.service.impl.StepExecutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StepExecutionListener {

    private final StepExecutionService stepExecutionService;

    @EventListener
    public void onExecuteStep(ExecuteStepEvent event) {
        log.info("接收到执行步骤事件: taskId={}, taskStatus={}, stepId={}, stepStatus={}",
                event.getTask() != null ? event.getTask().getId() : null,
                event.getTask() != null ? event.getTask().getStatus() : null,
                event.getStep() != null ? event.getStep().getId() : null,
                event.getStep() != null ? event.getStep().getStatus() : null);
        stepExecutionService.executeStep(event.getStep(), event.getTask());
    }
}
