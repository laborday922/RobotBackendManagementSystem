package com.ruoyi.data.clean.domain;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 数据清洗执行记录（对应 clean_execute_record 表）
 */
@Data
public class CleanExecuteRecord {

    private Long id;

    /**
     * 清洗任务ID（手动执行时可为空）
     */
    private Long taskId;

    /**
     * 执行模式：IMMEDIATE / SCHEDULED
     */
    private String executeMode;

    /**
     * 是否成功：1成功 0失败
     */
    private Integer success;

    /**
     * 失败原因
     */
    private String message;

    /**
     * 执行时间
     */
    private LocalDateTime runTime;

    private LocalDateTime createTime;
}
