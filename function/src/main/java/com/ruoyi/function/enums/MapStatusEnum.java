package com.ruoyi.function.enums;

/**
 * 地图状态枚举
 */
public enum MapStatusEnum {
    DISABLED("0", "禁用"),
    ENABLED("1", "启用");

    private final String code;
    private final String info;

    MapStatusEnum(String code, String info) {
        this.code = code;
        this.info = info;
    }

    public String getCode() {
        return code;
    }

    public String getInfo() {
        return info;
    }

    public static MapStatusEnum getByCode(String code) {
        for (MapStatusEnum value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }

    public static boolean isValid(String code) {
        return getByCode(code) != null;
    }
}