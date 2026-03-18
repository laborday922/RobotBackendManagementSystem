package com.ruoyi.data.metric.service.impl;

import com.ruoyi.data.metric.chartStrategy.ChartDataStrategy;
import com.ruoyi.data.metric.domain.sql.SQLBuilder;
import com.ruoyi.data.metric.mapper.MetricComputeMapper;
import com.ruoyi.data.metric.mapper.MetricDefinitionMapper;
import com.ruoyi.data.metric.mapper.po.MetricDefinitionPo;
import com.ruoyi.data.metric.service.MetricComputeService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Service
public class MetricComputeServiceImpl implements MetricComputeService {

    @Resource
    private MetricDefinitionMapper definitionMapper;

    @Resource
    private MetricComputeMapper computeMapper;

    @Resource
    private Map<String, ChartDataStrategy> chartStrategyMap;

    @Override
    public Double computeMetric(Long metricId){

        MetricDefinitionPo metric = definitionMapper.selectById(metricId);

        String sql = SQLBuilder.build(
                metric.getCalculationExpression(),
                metric.getDataSources()
        );

        return computeMapper.executeMetricSQL(sql);
    }

    @Override
    public Object getChartData(Long metricId){

        MetricDefinitionPo metric = definitionMapper.selectById(metricId);

        String sql = SQLBuilder.build(
                metric.getCalculationExpression(),
                metric.getDataSources()
        );

        List<Map<String,Object>> data = computeMapper.executeQuery(sql);

        ChartDataStrategy strategy =
                chartStrategyMap.get(metric.getChartType());

        return strategy.buildChartData(data);

    }

}