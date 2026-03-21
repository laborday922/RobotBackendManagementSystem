package com.ruoyi.robots.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import lombok.Data;

import java.util.Date;

@Data
public class RobotStatusDto {
    /** 机器人ID */
    private Long id;
    /** 机器人编号 */
    private String code;
    /** 在线状态（0-离线，1-在线，2-待激活） */
    private Integer status;

    /** 硬件状态（0-正常，1-警告，2-故障） */
    private Integer hardwareStatus;

    /** 任务状态（0-执行中，1-充电中，2-闲置，3-维护） */
    private Integer taskStatus;

    /** 当前电量（0-100） */
    private Integer battery;
    /** 备注 */
    private String note;

}
