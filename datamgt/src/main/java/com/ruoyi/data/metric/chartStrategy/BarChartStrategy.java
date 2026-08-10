package com.ruoyi.data.metric.chartStrategy;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component("bar")
public class BarChartStrategy implements ChartDataStrategy {

    @Override
    public Object buildChartData(List<Map<String, Object>> data) {

        Map<String, Object> result = new HashMap<>();

        List<String> xAxis = new ArrayList<>();
        List<Object> series = new ArrayList<>();

        for (Map<String, Object> row : data) {

            xAxis.add(String.valueOf(row.get("name")));

            series.add(row.get("value"));

        }

        result.put("xAxis", xAxis);
        result.put("series", series);

        return result;
    }

}
