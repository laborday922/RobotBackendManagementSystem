package com.ruoyi.mode.enums;

/**
 * 启用状态枚举
 */
public enum EnabledStatusEnum {
    DISABLED("0", "禁用"),
    ENABLED("1", "启用");

    private final String code;
    private final String desc;

    EnabledStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static EnabledStatusEnum getByCode(String code) {
        for (EnabledStatusEnum value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }
}