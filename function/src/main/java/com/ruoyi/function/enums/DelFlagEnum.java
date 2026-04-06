package com.ruoyi.function.enums;

/**
 * 删除标志枚举
 */
public enum DelFlagEnum {
    NORMAL("0", "正常"),
    DELETED("2", "已删除");

    private final String code;
    private final String info;

    DelFlagEnum(String code, String info) {
        this.code = code;
        this.info = info;
    }

    public String getCode() {
        return code;
    }

    public String getInfo() {
        return info;
    }

    public static String getNormal() {
        return NORMAL.code;
    }

    public static String getDeleted() {
        return DELETED.code;
    }
}
