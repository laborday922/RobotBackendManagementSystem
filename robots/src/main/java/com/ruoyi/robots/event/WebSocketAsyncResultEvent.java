package com.ruoyi.robots.event;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class WebSocketAsyncResultEvent extends ApplicationEvent {
    private final String traceId;
    private final boolean success;
    private final Object data;
    private final String errorMsg;

    public WebSocketAsyncResultEvent(Object source, String traceId, boolean success, Object data, String errorMsg) {
        super(source);
        this.traceId = traceId;
        this.success = success;
        this.data = data;
        this.errorMsg = errorMsg;
    }
}