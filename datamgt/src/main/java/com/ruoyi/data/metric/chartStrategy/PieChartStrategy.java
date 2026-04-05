package com.ruoyi.data.metric.chartStrategy;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component("pie")
public class PieChartStrategy implements ChartDataStrategy {

    @Override
    public Object buildChartData(List<Map<String,Object>> data){

        List<Map<String,Object>> result = new ArrayList<>();

        for(Map<String,Object> row : data){

            Map<String,Object> item = new HashMap<>();

            item.put("name",row.get("name"));
            item.put("value",row.get("value"));

            result.add(item);
        }

        return result;
    }

}