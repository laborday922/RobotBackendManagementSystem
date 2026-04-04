package com.ruoyi.data.ai.domain.bo;

import lombok.Data;

import java.util.Date;

@Data
public class Report {
    private Long id;
    private String reportName;
    private String reportType;
    private Date startDate;
    private Date endDate;
    private String status;
    private Date createdAt;
    private String content;   // 报告内容（用于业务层）
    // ... getters/setters
}