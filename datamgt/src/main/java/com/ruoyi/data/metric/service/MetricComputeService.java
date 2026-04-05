package com.ruoyi.data.metric.service;

public interface MetricComputeService {

    Double computeMetric(Long metricId);

    Object getChartData(Long metricId);

}