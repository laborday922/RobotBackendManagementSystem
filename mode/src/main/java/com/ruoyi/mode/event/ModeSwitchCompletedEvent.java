package com.ruoyi.mode.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ModeSwitchCompletedEvent extends ApplicationEvent {
    private final Long robotId;
    private final String traceId;
    private final boolean success;
    private final Object data;
    private final String errorMsg;

    public ModeSwitchCompletedEvent(Object source, Long robotId, String traceId, boolean success, Object data, String errorMsg) {
        super(source);
        this.robotId = robotId;
        this.traceId = traceId;
        this.success = success;
        this.data = data;
        this.errorMsg = errorMsg;
    }
}