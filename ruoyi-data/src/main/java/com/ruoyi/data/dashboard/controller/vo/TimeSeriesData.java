package com.ruoyi.data.dashboard.controller.vo;

import lombok.Data;

import java.util.List;

@Data
public class TimeSeriesData {

    private List<String> xAxis;

    private List<Integer> series;

}