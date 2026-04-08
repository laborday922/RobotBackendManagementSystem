package com.ruoyi.taskmgt.event;
import com.ruoyi.taskmgt.monitor.dto.RobotCallbackData;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class RobotCallbackEvent extends ApplicationEvent {
    private final String traceId;
    private final RobotCallbackData callbackData;

    public RobotCallbackEvent(Object source, String traceId, RobotCallbackData callbackData) {
        super(source);
        this.traceId = traceId;
        this.callbackData = callbackData;
    }
}