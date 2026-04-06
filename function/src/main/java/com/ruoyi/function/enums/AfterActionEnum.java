package com.ruoyi.function.enums;

/**
 * 讲解结束后动作枚举
 */
public enum AfterActionEnum {
    STAY("stay", "停留原地"),
    CHARGE("charge", "返回充电"),
    BACK_START("back_start", "返回起点");

    private final String code;
    private final String info;

    AfterActionEnum(String code, String info) {
        this.code = code;
        this.info = info;
    }

    public String getCode() {
        return code;
    }

    public String getInfo() {
        return info;
    }
}