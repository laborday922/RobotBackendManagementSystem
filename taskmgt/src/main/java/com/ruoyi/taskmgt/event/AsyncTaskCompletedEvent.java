package com.ruoyi.taskmgt.event;

import com.ruoyi.taskmgt.operation.dto.OperationResult;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class AsyncTaskCompletedEvent extends ApplicationEvent {
    private final Long stepId;
    private final OperationResult result;

    public AsyncTaskCompletedEvent(Object source, Long stepId, OperationResult result) {
        super(source);
        this.stepId = stepId;
        this.result = result;
    }
}