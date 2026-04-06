package com.ruoyi.function.enums;

/**
 * 导航播报类型枚举
 */
public enum NavVoiceTypeEnum {
    DEFAULT("default", "默认播报"),
    CUSTOM("custom", "自定义播报"),
    NONE("none", "无播报");

    private final String code;
    private final String info;

    NavVoiceTypeEnum(String code, String info) {
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