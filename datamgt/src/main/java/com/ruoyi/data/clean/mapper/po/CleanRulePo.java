package com.ruoyi.data.clean.mapper.po;

import com.ruoyi.common.core.domain.BaseEntity;
import lombok.Data;

import java.time.LocalTime;

@Data
public class CleanRulePo extends BaseEntity {

    private Long id;

    private String executeMode;

    private LocalTime runTime;

    private String applyDataSource;

    private String configJson;

    private Integer status;

    private String cronExpression;

    private Long tenantId;
}