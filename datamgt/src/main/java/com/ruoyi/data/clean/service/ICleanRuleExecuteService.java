package com.ruoyi.data.clean.service;

/**
 * 清洗规则执行服务接口
 */
public interface ICleanRuleExecuteService {

    /**
     * 执行清洗任务
     *
     * @param id 任务ID
     */
    void executeTask(Long id);
}