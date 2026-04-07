package com.ruoyi.taskmgt.event;

import com.ruoyi.taskmgt.domain.bo.Task;
import com.ruoyi.taskmgt.domain.bo.TaskStep;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ExecuteStepEvent extends ApplicationEvent {
    private final TaskStep step;
    private final Task task;

    public ExecuteStepEvent(Object source, TaskStep step, Task task) {
        super(source);
        this.step = step;
        this.task = task;
    }
}