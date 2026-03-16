package com.ruoyi.robots.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 机器人基础信息对象 robots
 * 
 * @author xiaocai
 * @date 2026-03-07
 */
@JsonInclude(JsonInclude.Include.NON_NULL)//仅序列化非空字段
public class Robot extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 机器人ID */
    private Long id;


    /** 机器人编号（唯一标识） */
    @Excel(name = "机器人编号", readConverterExp = "唯=一标识")
    private String code;

    /** 机器人名称 */
    @Excel(name = "机器人名称")
    private String name;

    /** 所属分组ID（逻辑外键） */
    @Excel(name = "所属分组ID", readConverterExp = "逻=辑外键")
    private Long groupId;

    /** 生产厂家 */
    @Excel(name = "生产厂家")
    private String manufacturer;

    /** 生产日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "生产日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date productionDate;

    /** 所属区域 */
    @Excel(name = "所属区域")
    private String area;

    /** 在线状态（0-离线，1-在线，2-待激活） */
    @Excel(name = "在线状态", readConverterExp = "0=-离线，1-在线，2-待激活")
    private String status;

    /** 硬件状态（0-正常，1-警告，2-故障） */
    @Excel(name = "硬件状态", readConverterExp = "0=-正常，1-警告，2-故障")
    private String hardwareStatus;

    /** 任务状态（0-执行中，1-充电中，2-闲置，3-维护） */
    @Excel(name = "任务状态", readConverterExp = "0=-执行中，1-充电中，2-闲置，3-维护")
    private String taskStatus;

    /** 当前电量（0-100） */
    @Excel(name = "当前电量", readConverterExp = "0=-100")
    private Integer battery;




    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "创建时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date createdAt;


    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getId()
    {
        return id;
    }

    public void setCode(String code) 
    {
        this.code = code;
    }

    public String getCode() 
    {
        return code;
    }

    public void setName(String name) 
    {
        this.name = name;
    }

    public String getName() 
    {
        return name;
    }

    public void setGroupId(Long groupId)
    {
        this.groupId = groupId;
    }

    public Long getGroupId()
    {
        return groupId;
    }

    public void setManufacturer(String manufacturer) 
    {
        this.manufacturer = manufacturer;
    }

    public String getManufacturer() 
    {
        return manufacturer;
    }

    public void setProductionDate(Date productionDate) 
    {
        this.productionDate = productionDate;
    }

    public Date getProductionDate() 
    {
        return productionDate;
    }

    public void setArea(String area) 
    {
        this.area = area;
    }

    public String getArea() 
    {
        return area;
    }

    public void setStatus(String status) 
    {
        this.status = status;
    }

    public String getStatus() 
    {
        return status;
    }

    public void setHardwareStatus(String hardwareStatus) 
    {
        this.hardwareStatus = hardwareStatus;
    }

    public String getHardwareStatus() 
    {
        return hardwareStatus;
    }

    public void setTaskStatus(String taskStatus) 
    {
        this.taskStatus = taskStatus;
    }

    public String getTaskStatus() 
    {
        return taskStatus;
    }

    public void setBattery(Integer battery)
    {
        this.battery = battery;
    }

    public Integer getBattery()
    {
        return battery;
    }



    public void setCreatedAt(Date createdAt) 
    {
        this.createdAt = createdAt;
    }

    public Date getCreatedAt() 
    {
        return createdAt;
    }



    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("code", getCode())
            .append("name", getName())
            .append("groupId", getGroupId())
            .append("manufacturer", getManufacturer())
            .append("productionDate", getProductionDate())
            .append("area", getArea())
            .append("status", getStatus())
            .append("hardwareStatus", getHardwareStatus())
            .append("taskStatus", getTaskStatus())
            .append("battery", getBattery())

            .append("createdAt", getCreatedAt())

            .toString();
    }
}
