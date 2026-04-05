package com.ruoyi.data.metric.controller;

import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.data.metric.domain.bo.MetricDefinition;
import com.ruoyi.data.metric.service.MetricDefinitionService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/metric")
public class MetricDefinitionController {

    @Resource
    private MetricDefinitionService service;

    @PostMapping("/create")
    public AjaxResult create(@RequestBody MetricDefinition metric) {
        Long id = service.create(metric);
        return AjaxResult.success(id);
    }

    @GetMapping("/{id}")
    public AjaxResult getById(@PathVariable Long id) {
        MetricDefinition metric = service.getById(id);
        return AjaxResult.success(metric);
    }

    @GetMapping("/list")
    public AjaxResult listAll() {
        List<MetricDefinition> list = service.listAll();
        return AjaxResult.success(list);
    }

    @PutMapping("/update")
    public AjaxResult update(@RequestBody MetricDefinition metric) {
        service.update(metric);
        return AjaxResult.success();
    }

    @DeleteMapping("/{id}")
    public AjaxResult delete(@PathVariable Long id) {
        service.delete(id);
        return AjaxResult.success();
    }

    //获取数据表字段
    @GetMapping("/fields")
    public AjaxResult getFields(@RequestParam String tableName) {
        List<String> fields = service.getFieldsByTableName(tableName);
        return AjaxResult.success(fields);
    }
}