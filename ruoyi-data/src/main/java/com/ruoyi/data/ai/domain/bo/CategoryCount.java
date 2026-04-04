package com.ruoyi.data.ai.domain.bo;

import lombok.Data;

@Data
public class CategoryCount {
    private String category;
    private Long count;
    private String suggestion;
    private Double percentage;
}