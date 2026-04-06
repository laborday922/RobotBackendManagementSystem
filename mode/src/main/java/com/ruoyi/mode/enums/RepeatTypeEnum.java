package com.ruoyi.mode.enums;

/**
 * 重复类型枚举
 */
public enum RepeatTypeEnum {
    ONCE("once", "仅一次"),
    DAILY("daily", "每天"),
    WEEKLY("weekly", "每周"),
    MONTHLY("monthly", "每月"),
    WEEKDAYS("weekdays", "工作日");

    private final String code;
    private final String desc;

    RepeatTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static RepeatTypeEnum getByCode(String code) {
        for (RepeatTypeEnum value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }
}