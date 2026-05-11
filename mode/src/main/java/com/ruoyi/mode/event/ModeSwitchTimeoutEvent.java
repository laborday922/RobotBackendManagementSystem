package com.ruoyi.mode.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ModeSwitchTimeoutEvent extends ApplicationEvent {
    private final Long robotId;
    private final String traceId;

    public ModeSwitchTimeoutEvent(Object source, Long robotId, String traceId) {
        super(source);
        this.robotId = robotId;
        this.traceId = traceId;
    }
}