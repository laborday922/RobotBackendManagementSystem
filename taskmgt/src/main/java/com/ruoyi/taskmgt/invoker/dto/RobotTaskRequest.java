package com.ruoyi.taskmgt.invoker.dto;


import lombok.Data;
import java.util.Map;

@Data
public class RobotTaskRequest {
    private String traceId;
    private Long operationId;
    private Map<String, Object> params;
    //private String callbackUrl;
    private String mode; // SYNC, ASYNC, CALLBACK
}