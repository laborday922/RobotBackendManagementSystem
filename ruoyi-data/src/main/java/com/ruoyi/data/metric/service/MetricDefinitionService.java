package com.ruoyi.data.metric.service;

import com.ruoyi.data.metric.domain.bo.MetricDefinition;

import java.util.List;

public interface MetricDefinitionService {

    Long create(MetricDefinition bo);

    MetricDefinition getById(Long id);

    List<MetricDefinition> listAll();

    void update(MetricDefinition bo);

    void delete(Long id);
}