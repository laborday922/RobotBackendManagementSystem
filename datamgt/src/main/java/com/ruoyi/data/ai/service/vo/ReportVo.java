package com.ruoyi.data.ai.service.vo;

import lombok.Data;

import java.util.Date;

@Data
public class ReportVo {

    private Long id;

    private String reportName;

    private String reportType;

    private String startDate;

    private String endDate;

    private String status;

    private Date createdAt;
}