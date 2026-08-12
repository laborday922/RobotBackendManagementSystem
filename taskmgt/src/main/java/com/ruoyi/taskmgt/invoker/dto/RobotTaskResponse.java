package com.ruoyi.taskmgt.invoker.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.Date;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RobotTaskResponse {
    private String traceId;
    private String interactionId;
    private String mode;
    private boolean success;
    private Object data;
    private String errorMsg;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date estimatedFinishTime;
    private boolean completed;
    private int progress;
    private String status;
}