package com.ruoyi.data.metric.service.impl;

import com.ruoyi.data.metric.chartStrategy.ChartDataStrategy;
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
        String sql = metric.getCalculationExpression();
        // 直接使用 executeQuery 获取多行结果
        List<Map<String, Object>> result = computeMapper.executeQuery(sql);
        if (result != null && !result.isEmpty()) {
            Map<String, Object> firstRow = result.get(0);
            // 尝试获取第一个数值类型的值
            for (Object value : firstRow.values()) {
                if (value instanceof Number) {
                    return ((Number) value).doubleValue();
                }
            }
        }
        return null;
    }

    @Override
    public Object getChartData(Long metricId){
        MetricDefinitionPo metric = definitionMapper.selectById(metricId);
        // 直接使用用户传入的完整 SQL
        String sql = metric.getCalculationExpression();
        List<Map<String, Object>> data = computeMapper.executeQuery(sql);
        ChartDataStrategy strategy = chartStrategyMap.get(metric.getChartType());
        return strategy.buildChartData(data);
    }

}