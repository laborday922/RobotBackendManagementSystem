package com.ruoyi.app.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 应用库对象 t_app_library
 * 
 * @author xiaocai
 * @date 2026-03-13
 */
@JsonInclude(JsonInclude.Include.NON_NULL)//仅序列化非空字段
public class TAppLibrary extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键，自增ID */
    private Long id;

    /** 应用唯一标识（业务ID） */
    @NotBlank(message = "应用编号不能为空")
    private String appId;

    /** 应用名称 */
    @NotBlank(message = "应用名称不能为空")
    private String appName;

    /** 应用类型：0(交互类)、1(控制类)、2(监控类) */
    @NotNull(message = "应用类型不能为空")
    private Long appType;

    /** 启用状态：1(启用)、0(禁用) */
    private Integer enabled;

    /** 应用描述 */
    private String description;

    /** 应用版本号（如1.0.0） */
    private String version;

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

    public void setAppName(String appName) 
    {
        this.appName = appName;
    }

    public String getAppName() 
    {
        return appName;
    }

    public void setAppType(Long appType) 
    {
        this.appType = appType;
    }

    public Long getAppType() 
    {
        return appType;
    }

    public void setEnabled(Integer enabled) 
    {
        this.enabled = enabled;
    }

    public Integer getEnabled() 
    {
        return enabled;
    }

    public void setDescription(String description) 
    {
        this.description = description;
    }

    public String getDescription() 
    {
        return description;
    }

    public void setVersion(String version) 
    {
        this.version = version;
    }

    public String getVersion() 
    {
        return version;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("appId", getAppId())
            .append("appName", getAppName())
            .append("appType", getAppType())
            .append("enabled", getEnabled())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .append("description", getDescription())
            .append("version", getVersion())
            .toString();
    }
}
