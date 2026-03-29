package com.ruoyi.taskmgt.domain.bo;

import lombok.Data;

import java.util.List;

@Data
public class TaskStepDefinition {
    private Integer order;           // 步骤序号
    private String name;             // 步骤名称
    private String description;      // 描述
    private Long apiId;              // API ID
    private List<ParamBinding> params; // 参数绑定列表
}