package com.ruoyi.function.enums;

/**
 * 点位类型枚举
 */
public enum PointTypeEnum {
    NORMAL("normal", "普通点位"),
    VIP("vip", "VIP点位"),
    SERVICE("service", "服务点位"),
    EXIT("exit", "出口点位"),
    TOILET("toilet", "卫生间"),
    ELEVATOR("elevator", "电梯"),
    STAIRS("stairs", "楼梯");

    private final String code;
    private final String info;

    PointTypeEnum(String code, String info) {
        this.code = code;
        this.info = info;
    }

    public String getCode() {
        return code;
    }

    public String getInfo() {
        return info;
    }

    public static PointTypeEnum getByCode(String code) {
        for (PointTypeEnum value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return NORMAL;
    }
}