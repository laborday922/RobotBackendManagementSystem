package com.ruoyi.taskmgt.service.vo;
import lombok.Data;
import java.util.List;

@Data
public class DynamicParamVo {
    private Long paramId;
    private String paramKey;
    private String paramName;
    private String paramType;
    private Boolean required;
    private List<ParamOption> options;
}

