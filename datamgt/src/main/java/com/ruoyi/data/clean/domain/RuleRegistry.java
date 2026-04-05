package com.ruoyi.data.clean.domain;

import com.alibaba.fastjson2.JSON;
import com.ruoyi.data.clean.domain.bo.CleanRule;
import com.ruoyi.data.clean.domain.bo.CleanRuleConfig;
import com.ruoyi.data.clean.domain.enums.DataSourceType;
import com.ruoyi.data.clean.domain.enums.ExecuteMode;
import com.ruoyi.data.clean.mapper.po.CleanRulePo;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 规则注册中心
 * 负责将数据库PO转换为领域对象
 */
public class RuleRegistry {

    private RuleRegistry() {}

    public static CleanRule build(CleanRulePo po) {

        if (po == null) {
            throw new RuntimeException("规则不存在");
        }

        CleanRule rule = new CleanRule();

        rule.setId(po.getId());
        rule.setRuleName("数据清洗任务-" + po.getId());
        rule.setDescription("系统内置规则");

        // 执行模式
        rule.setExecuteMode(
                ExecuteMode.valueOf(po.getExecuteMode())
        );

//        // 时间（你现在是String，需要转）
//        if (po.getRunTime() != null) {
//            rule.setRunTime(LocalTime.parse(po.getRunTime()));
//        }

        // 数据源解析
        rule.setDataSources(
                parseDataSources(po.getApplyDataSource())
        );

        // JSON 转领域配置对象
        CleanRuleConfig config = null;
        try {
            config = JSON.parseObject(po.getConfigJson(), CleanRuleConfig.class);
            System.out.println("Parsed config: " + config);
            System.out.println("raw config_json: [" + po.getConfigJson() + "]");
        } catch (Exception e) {
            e.printStackTrace();
        }

        rule.setConfig(config);

        return rule;
    }

    private static List<DataSourceType> parseDataSources(String sourceStr) {

        if (sourceStr == null || sourceStr.isEmpty()) {
            return List.of();
        }

        return Arrays.stream(sourceStr.split(","))
                .map(String::trim)
                .map(DataSourceType::valueOf)
                .collect(Collectors.toList());
    }
}