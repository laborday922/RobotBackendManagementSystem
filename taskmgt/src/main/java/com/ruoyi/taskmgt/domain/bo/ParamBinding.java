package com.ruoyi.taskmgt.domain.bo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ParamBinding {
    @JsonProperty("paramName")
    private String name;        // API 参数名
    private String paramLabel;     // 参数标签
    private String paramType;      // 参数类型
    @JsonProperty("sourceType")
    private String source;      // 来源类型：form_field, app_param, fixed, expression
    @JsonProperty("sourceValue")
    private String value;       // 根据 source 不同含义：字段key、参数key、固定值或表达式
    private boolean required;
}
