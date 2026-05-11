package com.ruoyi.taskmgt.invoker.dto;

import lombok.*;

import java.util.List;

@Data
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RobotParamDef {
    private String name;
    private String type;        // "select", "number", "string"
    private List<OptionDef> options;
}
