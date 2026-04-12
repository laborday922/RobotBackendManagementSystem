package com.ruoyi.mode.invoker.dto;

import lombok.Data;

/**
 * 模式切换响应
 */
@Data
public class ModeSwitchResponse {

    /** 追踪ID */
    private String traceId;

    /** 执行模式：SYNC/ASYNC/CALLBACK */
    private String mode;

    /** 是否成功 */
    private boolean success;

    /** 返回数据 */
    private Object data;

    /** 错误信息 */
    private String errorMsg;

    /** 预计完成时间 */
    private java.util.Date estimatedFinishTime;

    /** 是否完成 */
    private boolean completed;

    /** 进度 0-100 */
    private int progress;

    /** 状态 */
    private String status;

    public static ModeSwitchResponse syncSuccess(Object data) {
        ModeSwitchResponse resp = new ModeSwitchResponse();
        resp.setMode("SYNC");
        resp.setSuccess(true);
        resp.setData(data);
        resp.setCompleted(true);
        return resp;
    }

    public static ModeSwitchResponse syncError(String errorMsg) {
        ModeSwitchResponse resp = new ModeSwitchResponse();
        resp.setMode("SYNC");
        resp.setSuccess(false);
        resp.setErrorMsg(errorMsg);
        resp.setCompleted(true);
        return resp;
    }

    public static ModeSwitchResponse asyncSuccess(String traceId, java.util.Date estimatedFinishTime) {
        ModeSwitchResponse resp = new ModeSwitchResponse();
        resp.setMode("ASYNC");
        resp.setSuccess(true);
        resp.setTraceId(traceId);
        resp.setEstimatedFinishTime(estimatedFinishTime);
        return resp;
    }

    public static ModeSwitchResponse callbackMode(String traceId) {
        ModeSwitchResponse resp = new ModeSwitchResponse();
        resp.setMode("CALLBACK");
        resp.setTraceId(traceId);
        return resp;
    }
}