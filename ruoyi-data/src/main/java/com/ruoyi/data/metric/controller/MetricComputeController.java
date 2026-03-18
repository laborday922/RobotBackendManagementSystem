package com.ruoyi.data.metric.controller;

import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.data.metric.service.MetricComputeService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/metric")
public class MetricComputeController {

    @Resource
    private MetricComputeService computeService;

    /**
     * 计算指标
     */
    @PostMapping("/compute/{metricId}")
    public AjaxResult compute(@PathVariable Long metricId){

        Double result = computeService.computeMetric(metricId);

        return AjaxResult.success(result);
    }

    /**
     * 获取图表数据
     */
    @GetMapping("/chart/{metricId}")
    public AjaxResult chart(@PathVariable Long metricId){

        Object chartData = computeService.getChartData(metricId);

        return AjaxResult.success(chartData);
    }

}