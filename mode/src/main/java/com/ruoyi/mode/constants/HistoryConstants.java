package com.ruoyi.mode.constants;

/**
 * 历史记录常量
 */
public class HistoryConstants {

    // 操作类型
    public static final String OPERATION_TYPE_MODE_SWITCH = "mode-switch";
    public static final String OPERATION_TYPE_CONFIG_CHANGE = "config-change";
    public static final String OPERATION_TYPE_ALERT = "alert";
    public static final String OPERATION_TYPE_EMERGENCY = "emergency";
    public static final String OPERATION_TYPE_RESTART = "restart";
    public static final String OPERATION_TYPE_STATUS_REFRESH = "status-refresh";

    // 历史状态
    public static final String STATUS_SUCCESS = "success";
    public static final String STATUS_WARNING = "warning";
    public static final String STATUS_DANGER = "danger";

    // 清理策略
    public static final int DEFAULT_RETENTION_DAYS = 90;
    public static final int MAX_RETENTION_DAYS = 365;

    // 分页默认值
    public static final int DEFAULT_PAGE_NUM = 1;
    public static final int DEFAULT_PAGE_SIZE = 20;
}
