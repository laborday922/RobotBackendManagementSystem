package com.ruoyi.data.ai.controller.dto;

import lombok.Data;

@Data
public class ReportQueryDto {
    private Integer page = 1;
    private Integer size = 20;
    private String reportType;
    private String startDate;
    private String endDate;
    private String reportName;
}
