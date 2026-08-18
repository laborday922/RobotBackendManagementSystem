package com.ruoyi.data.clean.service;

/**
 * 清洗规则执行服务接口
 */
public interface ICleanRuleExecuteService {

    /**
     * 定时执行清洗任务
     *
     * @param taskId 任务ID
     */
    void executeScheduled(Long taskId);

    /**
     * 手动立即执行（不落任务，只产生执行记录）
     *
     * @param configJson      规则配置JSON
     * @param applyDataSource 应用数据源
     */
    void executeManual(String configJson, String applyDataSource);
}
