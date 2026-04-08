package com.ruoyi.taskmgt.invoker.dto;

import lombok.Data;

@Data
public class TaskStatusResponse {
    private boolean completed;
    private int progress;      // 0-100
    private String status;     // RUNNING, SUCCESS, FAILED
    private Object data;       // 完成时的结果数据
    private String errorMsg;
}
