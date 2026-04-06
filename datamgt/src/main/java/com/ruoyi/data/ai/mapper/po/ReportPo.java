package com.ruoyi.data.ai.mapper.po;

import lombok.Data;

import java.util.Date;

@Data
public class ReportPo {
    private Long id;
    private String reportName;
    private String reportType;
    private Date startDate;
    private Date endDate;
    private String status;
    private String taskId;
    private String fileUrl;
    private Date createdAt;
    private Long tenantId;
}