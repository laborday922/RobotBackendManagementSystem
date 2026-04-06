package com.ruoyi.data.dashboard.controller.vo;

import lombok.Data;

@Data
public class TimeSeriesItem {

    private String time;   // 时间点（如 2025-04-01）
    private Integer value; // 异常数量

}