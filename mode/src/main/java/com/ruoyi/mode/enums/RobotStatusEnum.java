package com.ruoyi.mode.enums;

/**
 * 机器人状态枚举
 */
public enum RobotStatusEnum {
    OFFLINE(0, "离线"),
    ONLINE(1, "在线"),
    PENDING_ACTIVATE(2, "待激活");

    private final Integer code;
    private final String desc;

    RobotStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static RobotStatusEnum getByCode(Integer code) {
        for (RobotStatusEnum value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }
}