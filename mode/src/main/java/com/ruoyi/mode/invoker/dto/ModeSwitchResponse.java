package com.ruoyi.mode.invoker.dto;

import lombok.Data;

/**
 * 模式切换响应
 */
@Data
public class ModeSwitchResponse {

    /** 执行模式：SYNC */
    private String mode;

    /** 是否成功 */
    private boolean success;

    /** 返回数据 */
    private Object data;

    /** 错误信息 */
    private String errorMsg;

    public static ModeSwitchResponse syncSuccess(Object data) {
        ModeSwitchResponse resp = new ModeSwitchResponse();
        resp.setMode("SYNC");
        resp.setSuccess(true);
        resp.setData(data);
        return resp;
    }

    public static ModeSwitchResponse syncError(String errorMsg) {
        ModeSwitchResponse resp = new ModeSwitchResponse();
        resp.setMode("SYNC");
        resp.setSuccess(false);
        resp.setErrorMsg(errorMsg);
        return resp;
    }
}
