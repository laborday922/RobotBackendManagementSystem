package com.ruoyi.app.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 应用更新记录对象 t_app_update
 * 
 * @author xiaocai
 * @date 2026-03-13
 */
@JsonInclude(JsonInclude.Include.NON_NULL)//仅序列化非空字段
public class TAppUpdate extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键，自增ID */
    private Long id;

    /** 关联应用库的app_id */
    private String appId;

    /** 更新版本号（如2.0.0） */
    private String updateVersion;

    /** 更新内容（更新说明） */
    private String updateContent;

    /** 更新状态：0(待更新)、1(更新中)、2(更新成功)、3(更新失败) */
    private Long updateStatus;

    /** 关联机器人ID */
    private String robotId;

    /** 更新失败原因（状态为3时填充） */
    private String failReason;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }

    public void setAppId(String appId) 
    {
        this.appId = appId;
    }

    public String getAppId() 
    {
        return appId;
    }

    public void setUpdateVersion(String updateVersion) 
    {
        this.updateVersion = updateVersion;
    }

    public String getUpdateVersion() 
    {
        return updateVersion;
    }

    public void setUpdateContent(String updateContent) 
    {
        this.updateContent = updateContent;
    }

    public String getUpdateContent() 
    {
        return updateContent;
    }

    public void setUpdateStatus(Long updateStatus) 
    {
        this.updateStatus = updateStatus;
    }

    public Long getUpdateStatus() 
    {
        return updateStatus;
    }

    public void setRobotId(String robotId) 
    {
        this.robotId = robotId;
    }

    public String getRobotId() 
    {
        return robotId;
    }

    public void setFailReason(String failReason) 
    {
        this.failReason = failReason;
    }

    public String getFailReason() 
    {
        return failReason;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("appId", getAppId())
            .append("updateVersion", getUpdateVersion())
            .append("updateContent", getUpdateContent())
            .append("updateStatus", getUpdateStatus())
            .append("robotId", getRobotId())
            .append("updateTime", getUpdateTime())
            .append("failReason", getFailReason())
            .append("createTime", getCreateTime())
            .toString();
    }
}
