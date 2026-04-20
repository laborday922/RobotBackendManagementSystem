package com.ruoyi.app.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 应用能力参数定义对象 t_app_param
 * 
 * @author xiaocai
 * @date 2026-03-28
 */
@JsonInclude(JsonInclude.Include.NON_NULL)//仅序列化非空字段
public class TAppParam extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** $column.columnComment */
    private Long id;

    /** $column.columnComment */
    @Excel(name = "${comment}", readConverterExp = "$column.readConverterExp()")
    private String appId;

    @Excel(name = "${comment}", readConverterExp = "$column.readConverterExp()")
    private Long apiId;

    /** $column.columnComment */
    @Excel(name = "${comment}", readConverterExp = "$column.readConverterExp()")
    private String paramKey;

    /** $column.columnComment */
    @Excel(name = "${comment}", readConverterExp = "$column.readConverterExp()")
    private String paramName;

    /** $column.columnComment */
    @Excel(name = "${comment}", readConverterExp = "$column.readConverterExp()")
    private String paramType;

    /** $column.columnComment */
    @Excel(name = "${comment}", readConverterExp = "$column.readConverterExp()")
    private String defaultValue;

    /** $column.columnComment */
    @Excel(name = "${comment}", readConverterExp = "$column.readConverterExp()")
    private String validationRule;

    /**'INPUT-用户输入, DYNAMIC-动态列表, FIXED-固定值'*/
    @Excel(name = "${comment}", readConverterExp = "$column.readConverterExp()")
    private String valueSource;

    @Excel(name = "${comment}", readConverterExp = "$column.readConverterExp()")
    private String dynamicConfig;
    /** $column.columnComment */
    @Excel(name = "${comment}", readConverterExp = "$column.readConverterExp()")
    private Date createdAt;

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

    public void setParamKey(String paramKey) 
    {
        this.paramKey = paramKey;
    }

    public String getParamKey() 
    {
        return paramKey;
    }

    public void setParamName(String paramName) 
    {
        this.paramName = paramName;
    }

    public String getParamName() 
    {
        return paramName;
    }

    public void setParamType(String paramType) 
    {
        this.paramType = paramType;
    }

    public String getParamType() 
    {
        return paramType;
    }

    public void setDefaultValue(String defaultValue) 
    {
        this.defaultValue = defaultValue;
    }

    public String getDefaultValue() 
    {
        return defaultValue;
    }

    public void setValidationRule(String validationRule) 
    {
        this.validationRule = validationRule;
    }

    public String getValidationRule() 
    {
        return validationRule;
    }

    public void setValueSource(String valueSource){this.valueSource=valueSource;}

    public String getValueSource(){return valueSource;}

    public void setDynamicConfig(String dynamicConfig){this.dynamicConfig=dynamicConfig;}

    public String getDynamicConfig(){return dynamicConfig;}
    public void setApiId(Long apiId) {
        this.apiId=apiId;
    }
    public Long getApiId(){return apiId;}
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
            .append("appId", getAppId())
            .append("apiId",getApiId())
            .append("paramKey", getParamKey())
            .append("paramName", getParamName())
            .append("paramType", getParamType())
            .append("defaultValue", getDefaultValue())
            .append("validationRule", getValidationRule())
            .append("valueSource",getValueSource())
            .append("dynamicConfig",getDynamicConfig())
            .append("createdAt", getCreatedAt())
            .toString();
    }


}
