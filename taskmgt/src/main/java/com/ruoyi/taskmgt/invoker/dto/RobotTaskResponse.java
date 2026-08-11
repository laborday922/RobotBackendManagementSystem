package com.ruoyi.taskmgt.invoker.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RobotTaskResponse {
    private String traceId;
    private String interactionId;
    private String mode;
    private boolean success;
    private Object data;
    private String errorMsg;
    private String estimatedFinishTime;
    private boolean completed;
    private int progress;
    private String status;
}