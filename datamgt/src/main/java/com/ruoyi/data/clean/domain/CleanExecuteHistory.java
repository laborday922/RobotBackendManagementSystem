package com.ruoyi.data.clean.domain;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CleanExecuteHistory {

    private Long id;

    /**
     * 执行模式：IMMEDIATE / SCHEDULE
     */
    private String executeMode;

    /**
     * 应用数据源
     */
    private String applyDataSource;

    /**
     * 执行时间
     */
    private LocalDateTime runTime;

    /**
     * 规则配置JSON
     */
    private String configJson;

    private LocalDateTime createTime;

    private String cronExpression;

    /**
     * 下次执行时间（根据 cron 计算，非数据库字段）
     */
    private LocalDateTime nextRunTime;

    private Long tenantId;
}