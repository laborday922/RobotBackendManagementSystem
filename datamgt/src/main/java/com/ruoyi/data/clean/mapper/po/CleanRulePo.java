package com.ruoyi.data.clean.mapper.po;

import com.ruoyi.common.core.domain.BaseEntity;
import lombok.Data;

@Data
public class CleanRulePo extends BaseEntity {

    private Long id;

    private String executeMode;

    private String applyDataSource;

    private String configJson;

    private String cronExpression;

    private Long tenantId;
}
