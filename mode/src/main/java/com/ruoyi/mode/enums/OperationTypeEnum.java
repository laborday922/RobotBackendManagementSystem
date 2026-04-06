package com.ruoyi.mode.enums;

/**
 * 操作类型枚举
 */
public enum OperationTypeEnum {
    MODE_SWITCH("mode-switch", "模式切换"),
    CONFIG_CHANGE("config-change", "配置变更"),
    ALERT("alert", "告警"),
    EMERGENCY("emergency", "紧急停止"),
    RESTART("restart", "重启"),
    STATUS_REFRESH("status-refresh", "状态刷新");

    private final String code;
    private final String desc;

    OperationTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static OperationTypeEnum getByCode(String code) {
        for (OperationTypeEnum value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }
}