package com.ruoyi.robots.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 机器人位置历史信息对象 robot_position_history
 * 
 * @author xiaocai
 * @date 2026-03-07
 */
@JsonInclude(JsonInclude.Include.NON_NULL)//仅序列化非空字段
public class RobotPositionHistory extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 自增主键ID */
    private Long id;

    /** 机器人ID */
    @Excel(name = "机器人ID")
    private String robotId;

    /** 记录时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    @Excel(name = "记录时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date recordTime;

    /** 位置区域（如：药房、走廊、病房区） */
    @Excel(name = "位置区域", readConverterExp = "如=：药房、走廊、病房区")
    private String locationArea;

    /** 具体位置（如：药房存储区、1号楼3层走廊） */
    @Excel(name = "具体位置", readConverterExp = "如=：药房存储区、1号楼3层走廊")
    private String specificLocation;

    /** 坐标X */
    @Excel(name = "坐标X")
    private BigDecimal coordinateX;

    /** 坐标Y */
    private BigDecimal coordinateY;

    /** 移动速度（单位：m/s） */
    @Excel(name = "移动速度", readConverterExp = "单=位：m/s")
    private BigDecimal moveSpeed;

    /** 状态描述（含主状态和子状态，如：待命 准备执行配送任务） */
    @Excel(name = "状态描述", readConverterExp = "含=主状态和子状态，如：待命,准=备执行配送任务")
    private String statusDesc;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }

    public void setRobotId(String robotId) 
    {
        this.robotId = robotId;
    }

    public String getRobotId() 
    {
        return robotId;
    }

    public void setRecordTime(Date recordTime) 
    {
        this.recordTime = recordTime;
    }

    public Date getRecordTime() 
    {
        return recordTime;
    }

    public void setLocationArea(String locationArea) 
    {
        this.locationArea = locationArea;
    }

    public String getLocationArea() 
    {
        return locationArea;
    }

    public void setSpecificLocation(String specificLocation) 
    {
        this.specificLocation = specificLocation;
    }

    public String getSpecificLocation() 
    {
        return specificLocation;
    }

    public void setCoordinateX(BigDecimal coordinateX) 
    {
        this.coordinateX = coordinateX;
    }

    public BigDecimal getCoordinateX() 
    {
        return coordinateX;
    }

    public void setCoordinateY(BigDecimal coordinateY) 
    {
        this.coordinateY = coordinateY;
    }

    public BigDecimal getCoordinateY() 
    {
        return coordinateY;
    }

    public void setMoveSpeed(BigDecimal moveSpeed) 
    {
        this.moveSpeed = moveSpeed;
    }

    public BigDecimal getMoveSpeed() 
    {
        return moveSpeed;
    }

    public void setStatusDesc(String statusDesc) 
    {
        this.statusDesc = statusDesc;
    }

    public String getStatusDesc() 
    {
        return statusDesc;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("robotId", getRobotId())
            .append("recordTime", getRecordTime())
            .append("locationArea", getLocationArea())
            .append("specificLocation", getSpecificLocation())
            .append("coordinateX", getCoordinateX())
            .append("coordinateY", getCoordinateY())
            .append("moveSpeed", getMoveSpeed())
            .append("statusDesc", getStatusDesc())
            .toString();
    }
}
