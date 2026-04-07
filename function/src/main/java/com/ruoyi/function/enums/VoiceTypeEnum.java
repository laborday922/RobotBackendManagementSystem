package com.ruoyi.function.enums;

/**
 * 音色类型枚举
 */
public enum VoiceTypeEnum {
    GENTLE_WOMAN("gentle_woman", "温柔女声"),
    BRIGHT_WOMAN("bright_woman", "明亮女声"),
    DEEP_MAN("deep_man", "深沉男声"),
    LIVELY_MAN("lively_man", "活泼男声"),
    CHILD("child", "童声"),
    CUSTOM("custom", "自定义");

    private final String code;
    private final String info;

    VoiceTypeEnum(String code, String info) {
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