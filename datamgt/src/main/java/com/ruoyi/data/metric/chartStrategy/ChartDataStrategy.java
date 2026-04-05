package com.ruoyi.data.metric.chartStrategy;

import java.util.List;
import java.util.Map;

public interface ChartDataStrategy {

    Object buildChartData(List<Map<String,Object>> data);

}