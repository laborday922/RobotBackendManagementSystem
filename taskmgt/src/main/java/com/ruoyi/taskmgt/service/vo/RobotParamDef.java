package com.ruoyi.taskmgt.service.vo;

import lombok.Data;

import java.util.List;

@Data
public class RobotParamDef {
    private String name;
    private String type;        // "select", "number", "string"
    private List<OptionDef> options;
}
