package com.ruoyi.mode.enums;

/**
 * 模式类型枚举
 */
public enum ModeTypeEnum {
    SYSTEM("system", "系统模式"),
    CUSTOM("custom", "自定义模式");

    private final String code;
    private final String desc;

    ModeTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static ModeTypeEnum getByCode(String code) {
        for (ModeTypeEnum value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }
}