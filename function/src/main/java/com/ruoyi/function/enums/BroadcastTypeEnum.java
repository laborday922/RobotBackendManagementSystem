package com.ruoyi.function.enums;

/**
 * 播报类型枚举
 */
public enum BroadcastTypeEnum {
    TEXT("text", "文本播报"),
    AUDIO("audio", "音频播报");

    private final String code;
    private final String info;

    BroadcastTypeEnum(String code, String info) {
        this.code = code;
        this.info = info;
    }

    public String getCode() {
        return code;
    }

    public String getInfo() {
        return info;
    }

    public static boolean isText(String code) {
        return TEXT.code.equals(code);
    }

    public static boolean isAudio(String code) {
        return AUDIO.code.equals(code);
    }
}
