package com.ruoyi.taskmgt.invoker.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
@Builder
@Getter
@Setter
public class RobotParamDef {
    private String name;
    private String type;        // "select", "number", "string"
    private List<OptionDef> options;
}
