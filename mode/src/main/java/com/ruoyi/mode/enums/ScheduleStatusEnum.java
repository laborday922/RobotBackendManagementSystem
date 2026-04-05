package com.ruoyi.mode.enums;

/**
 * 排程状态枚举
 */
public enum ScheduleStatusEnum {
    RUNNING("running", "运行中"),
    PAUSED("paused", "已暂停"),
    PENDING("pending", "等待执行"),
    COMPLETED("completed", "已完成"),
    FAILED("failed", "执行失败");

    private final String code;
    private final String desc;

    ScheduleStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static ScheduleStatusEnum getByCode(String code) {
        for (ScheduleStatusEnum value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }
}