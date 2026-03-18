package com.ruoyi.data.metric.controller;

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
    public Long create(@RequestBody MetricDefinition metric) {
        return service.create(metric);
    }

    @GetMapping("/{id}")
    public MetricDefinition getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @GetMapping("/list")
    public List<MetricDefinition> listAll() {
        return service.listAll();
    }

    @PutMapping("/update")
    public String update(@RequestBody MetricDefinition metric) {
        service.update(metric);
        return "success";
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        service.delete(id);
        return "success";
    }
}