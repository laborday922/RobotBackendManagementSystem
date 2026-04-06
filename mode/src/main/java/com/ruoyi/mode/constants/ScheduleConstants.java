package com.ruoyi.mode.constants;

/**
 * 排程模块常量
 */
public class ScheduleConstants {

    // 排程状态
    public static final String STATUS_RUNNING = "running";
    public static final String STATUS_PAUSED = "paused";
    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_COMPLETED = "completed";
    public static final String STATUS_FAILED = "failed";

    // 重复类型
    public static final String REPEAT_ONCE = "once";
    public static final String REPEAT_DAILY = "daily";
    public static final String REPEAT_WEEKLY = "weekly";
    public static final String REPEAT_MONTHLY = "monthly";
    public static final String REPEAT_WEEKDAYS = "weekdays";

    // 上次执行状态
    public static final String LAST_EXECUTE_SUCCESS = "success";
    public static final String LAST_EXECUTE_FAIL = "fail";

    // 默认持续时间（小时）
    public static final int DEFAULT_DURATION = 1;

    // 任务队列名称
    public static final String SCHEDULE_QUEUE_NAME = "schedule-execution-queue";
}
