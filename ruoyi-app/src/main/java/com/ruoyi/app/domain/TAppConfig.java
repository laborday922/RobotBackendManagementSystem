package com.ruoyi.app.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 应用配置对象 t_app_config
 * 
 * @author xiaocai
 * @date 2026-03-13
 */
@JsonInclude(JsonInclude.Include.NON_NULL)//仅序列化非空字段
public class TAppConfig extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键，自增ID */
    private Long id;

    /** 关联应用库的app_id */
    private String appId;

    /** 配置项键名（如：timeout、max_retry） */
    private String configKey;

    /** 配置项值（支持复杂内容） */
    private String configValue;

    /** 配置类型：0(字符串)、1(数字)、2(JSON)、3(布尔值) */
    private Long configType;

    /** 配置项展示名称（前端显示） */
    private String configLabel;

    /** 配置项描述 */
    private String configDesc;


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

    public void setConfigKey(String configKey) 
    {
        this.configKey = configKey;
    }

    public String getConfigKey() 
    {
        return configKey;
    }

    public void setConfigValue(String configValue) 
    {
        this.configValue = configValue;
    }

    public String getConfigValue() 
    {
        return configValue;
    }

    public void setConfigType(Long configType) 
    {
        this.configType = configType;
    }

    public Long getConfigType() 
    {
        return configType;
    }

    public void setConfigLabel(String configLabel) 
    {
        this.configLabel = configLabel;
    }

    public String getConfigLabel() 
    {
        return configLabel;
    }

    public void setConfigDesc(String configDesc) 
    {
        this.configDesc = configDesc;
    }

    public String getConfigDesc() 
    {
        return configDesc;
    }


    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("appId", getAppId())
            .append("configKey", getConfigKey())
            .append("configValue", getConfigValue())
            .append("configType", getConfigType())
            .append("configLabel", getConfigLabel())
            .append("configDesc", getConfigDesc())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
