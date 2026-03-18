package com.ruoyi.data.clean.cleanExecuteStrategy;

import com.ruoyi.data.clean.domain.bo.CleanRule;
import com.ruoyi.data.clean.domain.bo.CleanRuleConfig;

import java.util.ArrayList;
import java.util.List;

public class StrategyFactory {

    public static List<CleanStrategy> build(CleanRule rule) {

        List<CleanStrategy> strategies = new ArrayList<>();

        CleanRuleConfig config = rule.getConfig();

        if (config.getFieldIntegrityType() != null) {
            strategies.add(
                    new FieldIntegrityStrategy(config.getFieldIntegrityType())
            );
        }

        if (config.getOutlierStrategyType() != null) {
            strategies.add(
                    new OutlierStrategy(config.getOutlierStrategyType())
            );
        }

        if (config.getTimeFormatType() != null) {
            strategies.add(
                    new TimeFormatStrategy(config.getTimeFormatType())
            );
        }

        if (config.getDuplicateHandlingType() != null) {
            strategies.add(
                    new DuplicateHandlingStrategy(config.getDuplicateHandlingType())
            );
        }

        if (config.getTextCleaningType() != null) {
            strategies.add(
                    new TextCleaningStrategy(config.getTextCleaningType())
            );
        }

        if (config.getStatusMappingType() != null) {
            strategies.add(
                    new StatusMappingStrategy(config.getStatusMappingType())
            );
        }

        return strategies;
    }
}