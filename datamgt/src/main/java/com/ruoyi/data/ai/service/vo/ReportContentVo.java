package com.ruoyi.data.ai.service.vo;

import lombok.Data;

import java.util.Date;

@Data
public class ReportContentVo {

    private Long reportId;

    private String content;

    private String summary;

    private String highlights;

    private Date createdAt;
}