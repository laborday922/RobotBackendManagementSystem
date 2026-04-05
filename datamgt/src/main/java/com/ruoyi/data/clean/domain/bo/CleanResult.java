package com.ruoyi.data.clean.domain.bo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CleanResult {

    private Long sourceId;
    private Long taskId;
    private Long configId;

    private String rawContent;
    private String cleanContent;

    private String statusLabel;

    private LocalDateTime cleanTime;
}