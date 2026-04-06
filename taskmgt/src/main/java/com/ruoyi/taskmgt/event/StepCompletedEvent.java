package com.ruoyi.taskmgt.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class StepCompletedEvent extends ApplicationEvent {
    private final Long taskId;
    private final Long stepId;
    private final boolean success;

    public StepCompletedEvent(Object source, Long taskId, Long stepId, boolean success) {
        super(source);
        this.taskId = taskId;
        this.stepId = stepId;
        this.success = success;
    }
}