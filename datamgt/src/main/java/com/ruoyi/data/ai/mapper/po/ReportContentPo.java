package com.ruoyi.data.ai.mapper.po;

import lombok.Data;

import java.util.Date;

@Data
public class ReportContentPo {
    private Long id;
    private Long reportId;
    private String content;
    private String summary;      // 新增
    private String highlights;   // 新增
    private Date createdAt;
    private Long tenantId;
}