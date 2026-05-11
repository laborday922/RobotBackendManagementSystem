package com.ruoyi.robots.event;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;
@Getter
@Setter
public class RobotConnectedEvent  extends ApplicationEvent {
    private final Long robotId;
    private final boolean isConnected;

    public RobotConnectedEvent(Object source,Long robotId,boolean isConnected) {
        super(source);
        this.robotId =robotId;
        this.isConnected=isConnected;
    }
}
