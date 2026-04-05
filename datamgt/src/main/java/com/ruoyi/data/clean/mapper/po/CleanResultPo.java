package com.ruoyi.data.clean.mapper.po;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CleanResultPo {

    private Long sourceId;
    private Long taskId;
    private Long configId;

    private String rawContent;
    private String cleanContent;

    private String statusLabel;

    private LocalDateTime cleanTime;
}