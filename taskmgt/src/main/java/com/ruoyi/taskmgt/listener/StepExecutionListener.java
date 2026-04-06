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
        stepExecutionService.executeStep(event.getStep(), event.getTask());
    }
}