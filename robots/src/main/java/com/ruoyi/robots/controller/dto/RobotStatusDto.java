package com.ruoyi.robots.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import lombok.Data;

import java.math.BigDecimal;
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


    private String locationArea;

    /** 具体位置（如：药房存储区、1号楼3层走廊） */
    private String specificLocation;

    /** 坐标X */
    private BigDecimal coordinateX;

    /** 坐标Y */
    private BigDecimal coordinateY;

    /** 移动速度（单位：m/s） */
    private BigDecimal moveSpeed;

    /** 状态描述（含主状态和子状态，如：待命 准备执行配送任务） */
    private String statusDesc;

    /** 最后一次心跳时间 */
    private Date lastHeartbeatTime;
}
