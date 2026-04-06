package com.ruoyi.function.enums;

/**
 * 讲解内容类型枚举
 */
public enum ContentTypeEnum {
    TEXT("text", "文本"),
    AUDIO("audio", "音频"),
    IMAGE("image", "图片"),
    VIDEO("video", "视频"),
    MIXED("mixed", "混合");

    private final String code;
    private final String info;

    ContentTypeEnum(String code, String info) {
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