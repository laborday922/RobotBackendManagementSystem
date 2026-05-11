package com.ruoyi.taskmgt.domain.bo;

import lombok.Data;
import java.util.Map;

@Data
public class ApiParamDef {
    private String type;                // number, string, boolean
    private Boolean required;
    private String description;
    private String valueSource;         // INPUT, DYNAMIC, FIXED, APP_PARAM
    private Object defaultValue;
    private Map<String, Object> dynamicConfig;
}
