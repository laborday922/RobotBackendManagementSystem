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

    /**
     * 执行数据清洗主流程。
     *
     * 根据配置文件中定义的策略类型，依次对每条原始数据进行：
     * 文本清洗、去重判断、状态映射，最后将清洗结果封装并存入上下文结果集。
     *
     * @param context 清洗上下文，包含任务ID、配置ID、原始数据列表以及用于存储结果的容器。
     */
    public void execute(DataContext context) {

        // ==================== 策略获取阶段 ====================
        // 从配置对象（config）中读取用户在界面上选择或预设的三种清洗策略的标识（枚举或字符串）
        var textType = config.getTextCleaning();
        var statusType = config.getStatusMapping();
        var duplicateType = config.getDuplicateHandling();

        // 初始化策略，根据策略类型动态创建对应的策略实例（策略模式），每个策略负责独立的清洗环节
        var textStrategy = new com.ruoyi.data.clean.cleanExecuteStrategy.TextCleaningStrategy(textType);
        var statusStrategy = new com.ruoyi.data.clean.cleanExecuteStrategy.StatusMappingStrategy(statusType);
        var duplicateStrategy = new com.ruoyi.data.clean.cleanExecuteStrategy.DuplicateHandlingStrategy(duplicateType);

        // 获取本次任务需要处理的原始数据行（例如从数据库或Excel读取的列表）
        var rawData = context.getRawData();

        for (var row : rawData) {
            // 从当前行数据中提取三个关键字段：源记录ID、待清洗文本、当前状态码
            // 注意：id 字段可能来自数据库自增列，类型为数值（Integer/Long），此处强制转为 Long
            Long sourceId = ((Number) row.get("id")).longValue();
            String text = (String) row.get("evaluation_text");
            Integer status = (Integer) row.get("status");

            if (text == null) continue;

            // 1️.文本处理
            String cleanedText = textStrategy.process(text, context);

            // 2️.去重
            if (!duplicateStrategy.shouldKeep(cleanedText)) {
                continue;
            }

            // 3️.状态映射
            String statusLabel = statusStrategy.process(status, context);

            // 4️.构建结果
            var result = new com.ruoyi.data.clean.domain.bo.CleanResult();
            result.setSourceId(sourceId);
            result.setTaskId(context.getTaskId());
            result.setConfigId(context.getConfigId());
            result.setRawContent(text);
            result.setCleanContent(cleanedText);
            result.setStatusLabel(statusLabel);
            result.setCleanTime(java.time.LocalDateTime.now());

            // 将构建好的结果对象添加到上下文中，后续统一写入数据库
            context.addResult(result);
        }
    }
}