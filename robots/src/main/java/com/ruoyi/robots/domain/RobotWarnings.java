package com.ruoyi.robots.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 机器人状态预警对象 robot_warnings
 * 
 * @author xiaocai
 * @date 2026-03-07
 */
@JsonInclude(JsonInclude.Include.NON_NULL)//仅序列化非空字段
public class RobotWarnings extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 预警ID */
    private Long id;

    /** 关联机器人ID（逻辑外键） */
    @Excel(name = "关联机器人ID", readConverterExp = "逻=辑外键")
    private Long robotId;

    /** 预警类型（0-低电量，1-硬件故障，2-硬件异常，3-离线） */
    @Excel(name = "预警类型", readConverterExp = "0=-低电量，1-硬件故障，2-硬件异常，3-离线")
    private String warningType;

    /** 预警描述内容 */
    @Excel(name = "预警描述内容")
    private String warningContent;

    /** 预警级别（0-提示，1-警告，2-错误） */
    @Excel(name = "预警级别", readConverterExp = "0=-提示，1-警告，2-错误")
    private String warningLevel;

    /** 预警状态（0-待处理，1-已解决，2-已忽略） */
    @Excel(name = "预警状态", readConverterExp = "0=-待处理，1-已解决，2-已忽略")
    private String status;

    /** 处理完成时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    @Excel(name = "处理完成时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date resolveTime;

    /** 处理人 */
    @Excel(name = "处理人")
    private String resolveUser;

    /** 处理备注 */
    @Excel(name = "处理备注")
    private String resolveNote;

    /** 预警创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    @Excel(name = "预警创建时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date createdAt;

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getId()
    {
        return id;
    }

    public void setRobotId(Long robotId)
    {
        this.robotId = robotId;
    }

    public Long getRobotId()
    {
        return robotId;
    }

    public void setWarningType(String warningType) 
    {
        this.warningType = warningType;
    }

    public String getWarningType() 
    {
        return warningType;
    }

    public void setWarningContent(String warningContent) 
    {
        this.warningContent = warningContent;
    }

    public String getWarningContent() 
    {
        return warningContent;
    }

    public void setWarningLevel(String warningLevel) 
    {
        this.warningLevel = warningLevel;
    }

    public String getWarningLevel() 
    {
        return warningLevel;
    }

    public void setStatus(String status) 
    {
        this.status = status;
    }

    public String getStatus() 
    {
        return status;
    }

    public void setResolveTime(Date resolveTime) 
    {
        this.resolveTime = resolveTime;
    }

    public Date getResolveTime() 
    {
        return resolveTime;
    }

    public void setResolveUser(String resolveUser) 
    {
        this.resolveUser = resolveUser;
    }

    public String getResolveUser() 
    {
        return resolveUser;
    }

    public void setResolveNote(String resolveNote) 
    {
        this.resolveNote = resolveNote;
    }

    public String getResolveNote() 
    {
        return resolveNote;
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
            .append("robotId", getRobotId())
            .append("warningType", getWarningType())
            .append("warningContent", getWarningContent())
            .append("warningLevel", getWarningLevel())
            .append("status", getStatus())
            .append("resolveTime", getResolveTime())
            .append("resolveUser", getResolveUser())
            .append("resolveNote", getResolveNote())
            .append("createdAt", getCreatedAt())
            .toString();
    }
}
