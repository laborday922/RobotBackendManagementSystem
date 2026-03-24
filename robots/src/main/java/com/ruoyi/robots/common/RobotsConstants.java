package com.ruoyi.robots.common;

public class RobotsConstants {

    //预警状态（0-待处理，1-已解决，2-已忽略
    public static final String UNRESOLVED="0";
    public static final String RESOLVED="1";

    /** 任务状态（0-执行中，1-充电中，2-闲置，3-维护） */
    public static final Integer EXECUTING=0;
    public static final Integer CHARGING=1;
    public static final Integer IDLE=2;
    public static final Integer MAINTENANCE=3;

    public static final String GROUP_BY_RELATED_BY_ROBOT = "分组被机器人关联";
    public static final String ROBOT_CODE_HAS_EXISTED = "机器人编号已存在";
    public static final String GROUP_NAME_HAS_EXISTED = "分组名称已存在";
    public static final String ROBOT_ID_NO_EXIST = "机器人id不存在";

    public static final Integer NORMAL=0;


    public static final String PROMPT="0";
    public static final String WARNING = "1";
    public static final String ERROR = "2";
}
