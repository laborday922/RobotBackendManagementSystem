package com.ruoyi.data.clean.domain.bo;

import com.ruoyi.data.clean.domain.context.DataContext;
import com.ruoyi.data.clean.domain.enums.DataSourceType;
import com.ruoyi.data.clean.domain.enums.ExecuteMode;
import lombok.Data;

import java.time.LocalTime;
import java.util.List;

@Data
public class CleanRule {

    private Long id;

    private String ruleName;

    private String description;

    private ExecuteMode executeMode;

    private LocalTime runTime;

    private List<DataSourceType> dataSources;

    private CleanRuleConfig config;

    private String cronExpression;

    /* ========= 领域行为 ========= */

    public boolean isScheduled() {
        return ExecuteMode.SCHEDULED.equals(this.executeMode);
    }

    public void execute(DataContext context) {

        // 从 config 获取策略类型
        var textType = config.getTextCleaning();
        var statusType = config.getStatusMapping();
        var duplicateType = config.getDuplicateHandling();

        // 初始化策略
        var textStrategy = new com.ruoyi.data.clean.cleanExecuteStrategy.TextCleaningStrategy(textType);
        var statusStrategy = new com.ruoyi.data.clean.cleanExecuteStrategy.StatusMappingStrategy(statusType);
        var duplicateStrategy = new com.ruoyi.data.clean.cleanExecuteStrategy.DuplicateHandlingStrategy(duplicateType);

        var rawData = context.getRawData();

        for (var row : rawData) {

            Long sourceId = ((Number) row.get("id")).longValue();
            String text = (String) row.get("evaluation_text");
            Integer status = (Integer) row.get("status");

            if (text == null) continue;

            // 1️⃣ 文本处理
            String cleanedText = textStrategy.process(text, context);

            // 2️⃣ 去重
            if (!duplicateStrategy.shouldKeep(cleanedText)) {
                continue;
            }

            // 3️⃣ 状态映射
            String statusLabel = statusStrategy.process(status, context);

            // 4️⃣ 构建结果（唯一出口🔥）
            var result = new com.ruoyi.data.clean.domain.bo.CleanResult();
            result.setSourceId(sourceId);
            result.setTaskId(context.getTaskId());
            result.setConfigId(context.getConfigId());
            result.setRawContent(text);
            result.setCleanContent(cleanedText);
            result.setStatusLabel(statusLabel);
            result.setCleanTime(java.time.LocalDateTime.now());

            context.addResult(result);
        }
    }
}