package com.ruoyi.data.clean.domain.bo;

import com.ruoyi.data.clean.domain.enums.*;
import lombok.Data;

//规则配置的领域模型
@Data
public class CleanRuleConfig {

    private FieldIntegrityType fieldIntegrityType;

    private OutlierStrategyType outlierStrategyType;

    private TimeFormatType timeFormatType;

    private TextCleaningType textCleaning;

    private StatusMappingType statusMapping;

    private DuplicateHandlingType duplicateHandling;

    // getter / setter
}