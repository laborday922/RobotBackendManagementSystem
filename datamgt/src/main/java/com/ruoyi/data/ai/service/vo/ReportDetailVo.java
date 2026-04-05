package com.ruoyi.data.ai.service.vo;

import lombok.Data;

@Data
public class ReportDetailVo {

    // ===== 基本信息 =====
    private Long id;
    private String reportName;
    private String reportType;
    private String startDate;
    private String endDate;
    private String status;
    private String createTime;

    // ===== 内容 =====
    private String content;
    private String summary;
    private String highlights;
}