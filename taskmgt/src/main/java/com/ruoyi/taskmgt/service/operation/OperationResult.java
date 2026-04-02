package com.ruoyi.taskmgt.service.operation;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class OperationResult {
    private boolean success;
    private String code;
    private String message;
    private Object data;
    private OperationType type;  // SYNC/ASYNC/CALLBACK
    private String traceId;      // 异步追踪ID
    private Long costTimeMs;
    private boolean completed;      // 是否已完成（用于状态查询）
    private int progress;           // 进度0-100
    private Date estimatedFinishTime;  // 预计完成时间
}
