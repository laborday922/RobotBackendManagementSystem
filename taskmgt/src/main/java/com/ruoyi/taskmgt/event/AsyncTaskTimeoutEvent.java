package com.ruoyi.taskmgt.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class AsyncTaskTimeoutEvent extends ApplicationEvent {
    private final Long stepId;

    public AsyncTaskTimeoutEvent(Object source, Long stepId) {
        super(source);
        this.stepId = stepId;
    }
}