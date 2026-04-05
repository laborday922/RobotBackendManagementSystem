package com.ruoyi.data.metric.mapper.po;

import com.ruoyi.common.core.domain.BaseEntity;
import lombok.Data;

import java.util.Date;

@Data
public class MetricDefinitionPo extends BaseEntity {

    private Long id;

    private String metricName;

    private String category;

    private String description;

    private String dataSources;

    private String selectedFields;

    private String calculationExpression;

    private String updateFrequency;

    private String chartType;

    private Integer enableAlert;

    private Double alertThreshold;

    private String tags;

    private Long creatorId;

    private Date createTime;

    private Date updateTime;

}