package com.ruoyi.data.metric.chartStrategy;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component("line")
public class LineChartStrategy implements ChartDataStrategy {

    @Override
    public Object buildChartData(List<Map<String,Object>> data){

        Map<String,Object> result = new HashMap<>();

        List<String> xAxis = new ArrayList<>();
        List<Object> series = new ArrayList<>();

        for(Map<String,Object> row : data){

            // 与柱状图/饼图保持一致，优先取 name；兼容旧的 time 列
            Object x = row.get("name");
            if (x == null) {
                x = row.get("time");
            }

            xAxis.add(String.valueOf(x));

            series.add(row.get("value"));

        }

        result.put("xAxis",xAxis);
        result.put("series",series);

        return result;
    }

}