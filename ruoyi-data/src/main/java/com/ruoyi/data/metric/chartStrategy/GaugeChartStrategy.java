package com.ruoyi.data.metric.chartStrategy;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component("gauge")
public class GaugeChartStrategy implements ChartDataStrategy {

    @Override
    public Object buildChartData(List<Map<String,Object>> data){

        if(data.isEmpty()){
            return 0;
        }

        return data.get(0).get("value");

    }

}
