package com.ruoyi.taskmgt.invoker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RobotParamsResponse {
    private List<RobotParamDef> params;
}
