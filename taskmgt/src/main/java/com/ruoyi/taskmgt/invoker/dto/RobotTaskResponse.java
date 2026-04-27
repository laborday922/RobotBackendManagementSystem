package com.ruoyi.taskmgt.invoker.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.Date;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RobotTaskResponse {
    private String traceId;
    private String mode;
    private boolean success;
    private Object data;
    private String errorMsg;
    private Date estimatedFinishTime;
    private boolean completed;
    private int progress;
    private String status;
}