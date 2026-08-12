package com.ruoyi.taskmgt.invoker.dto;


import lombok.Data;
import java.util.Map;

@Data
public class RobotTaskRequest {
    private String traceId;
    private String interactionId;
    private Long operationId;
    private Map<String, Object> params;
    private Boolean latest;   // 是否为最后一个步骤
    //private String callbackUrl;
    private String mode; // SYNC, ASYNC, CALLBACK
}