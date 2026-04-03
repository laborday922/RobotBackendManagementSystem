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

    /**
     * 执行状态：0失败 1成功 2运行中
     */
    private Integer status;

    private LocalDateTime createTime;

    private String cronExpression;
}