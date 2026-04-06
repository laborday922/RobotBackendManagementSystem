package com.ruoyi.data.ai.domain.bo;

import lombok.Data;

import java.util.List;

@Data
public class TimeSeriesData {
    private List<String> timeList;
    private List<Long> valueList;
}