package com.ruoyi.data.ai.controller.dto;

import lombok.Data;

@Data
public class ReportGenerateRequestDto {
    private String reportType;           // 报告类型，如“服务质量评估报告”
    private String startDate;            // 开始日期，格式 yyyy-MM-dd
    private String endDate;              // 结束日期，格式 yyyy-MM-dd
    private String analysisDimension;    // 分析维度，如 "user_satisfaction"
    private String customPrompt;         // 自定义要求 prompt（用户自然语言）
    private String reportDepth;          // 报告深度类型：summary（摘要版）或 standard（标准版）
}