package com.ruoyi.taskmgt.monitor.dto;

import lombok.Data;
import java.util.Date;

@Data
public class RobotCallbackData {
    private boolean success;
    private String errorMsg;
    private Object data;
    private String traceId;
    private String interactionId;
    private Long robotId;
    private Date timestamp;
}