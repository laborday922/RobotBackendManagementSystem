package com.ruoyi.taskmgt.common.constants;

/**
 * 任务日志事件类型常量
 */
public interface TaskLogEventType {
    String TASK_CREATE = "TASK_CREATE";
    String TASK_START = "TASK_START";
    String TASK_PAUSE = "TASK_PAUSE";
    String TASK_RESUME = "TASK_RESUME";
    String TASK_TERMINATE = "TASK_TERMINATE";
    String TASK_COMPLETE = "TASK_COMPLETE";
    String TASK_CANCEL = "TASK_CANCEL";
    String TASK_DELETE = "TASK_DELETE";
    String TASK_UPDATE = "TASK_UPDATE";
    String TASK_ERROR = "TASK_ERROR";
    String STEP_START = "STEP_START";
    String STEP_COMPLETE = "STEP_COMPLETE";
    String STEP_PAUSE = "STEP_PAUSE";
    String STEP_RESUME = "STEP_RESUME";
    String STEP_TERMINATE = "STEP_TERMINATE";
    String STEP_ERROR = "STEP_ERROR";
    String ROBOT_STATUS_CHANGE = "ROBOT_STATUS_CHANGE";
    String TASK_PENDING = "TASK_PENDING";
}