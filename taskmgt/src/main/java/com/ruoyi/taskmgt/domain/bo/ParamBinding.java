package com.ruoyi.taskmgt.domain.bo;

import lombok.Data;

@Data
public class ParamBinding {
    private String name;        // API 参数名
    private String source;      // 来源类型：form_field, app_param, fixed, expression
    private String value;       // 根据 source 不同含义：字段key、参数key、固定值或表达式
}
