package com.ruoyi.data.metric.domain.bo;

import lombok.Data;

import java.util.List;

@Data
public class MetricDefinition {

    private Long id;

    private String metricName;

    private String category;

    private String description;

    private List<String> dataSources;

    private List<String> selectedFields;

    private String calculationExpression;

    private String updateFrequency;

    private String chartType;

    private Boolean enableAlert;

    private Double alertThreshold;

    private List<String> tags;

}
