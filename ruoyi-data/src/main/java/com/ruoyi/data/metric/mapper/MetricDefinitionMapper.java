package com.ruoyi.data.metric.mapper;

import com.ruoyi.data.metric.mapper.po.MetricDefinitionPo;

import java.util.List;

public interface MetricDefinitionMapper {

    int insert(MetricDefinitionPo po);

    MetricDefinitionPo selectById(Long id);

    List<MetricDefinitionPo> selectAll();

    int update(MetricDefinitionPo po);

    int delete(Long id);
}
