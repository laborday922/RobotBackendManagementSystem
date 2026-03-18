package com.ruoyi.data.metric.service.impl;

import com.alibaba.fastjson2.JSON;
import com.ruoyi.data.metric.domain.bo.MetricDefinition;
import com.ruoyi.data.metric.mapper.MetricDefinitionMapper;
import com.ruoyi.data.metric.mapper.po.MetricDefinitionPo;
import com.ruoyi.data.metric.service.MetricDefinitionService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MetricDefinitionServiceImpl implements MetricDefinitionService {

    @Resource
    private MetricDefinitionMapper mapper;

    @Override
    public Long create(MetricDefinition bo) {

        MetricDefinitionPo po = convertToPO(bo);

        mapper.insert(po);

        return po.getId();
    }

    @Override
    public MetricDefinition getById(Long id) {

        MetricDefinitionPo po = mapper.selectById(id);

        return convertToBO(po);
    }

    @Override
    public List<MetricDefinition> listAll() {

        return mapper.selectAll()
                .stream()
                .map(this::convertToBO)
                .collect(Collectors.toList());
    }

    @Override
    public void update(MetricDefinition bo) {

        MetricDefinitionPo po = convertToPO(bo);

        mapper.update(po);
    }

    @Override
    public void delete(Long id) {

        mapper.delete(id);
    }

    private MetricDefinitionPo convertToPO(MetricDefinition bo) {

        MetricDefinitionPo po = new MetricDefinitionPo();

        po.setId(bo.getId());
        po.setMetricName(bo.getMetricName());
        po.setCategory(bo.getCategory());
        po.setDescription(bo.getDescription());

        po.setDataSources(JSON.toJSONString(bo.getDataSources()));
        po.setSelectedFields(JSON.toJSONString(bo.getSelectedFields()));

        po.setCalculationExpression(bo.getCalculationExpression());
        po.setUpdateFrequency(bo.getUpdateFrequency());
        po.setChartType(bo.getChartType());

        po.setEnableAlert(Boolean.TRUE.equals(bo.getEnableAlert()) ? 1 : 0);
        po.setAlertThreshold(bo.getAlertThreshold());

        po.setTags(JSON.toJSONString(bo.getTags()));

        return po;
    }

    private MetricDefinition convertToBO(MetricDefinitionPo po) {

        MetricDefinition bo = new MetricDefinition();

        bo.setId(po.getId());
        bo.setMetricName(po.getMetricName());
        bo.setCategory(po.getCategory());
        bo.setDescription(po.getDescription());

        bo.setDataSources(JSON.parseArray(po.getDataSources(), String.class));
        bo.setSelectedFields(JSON.parseArray(po.getSelectedFields(), String.class));

        bo.setCalculationExpression(po.getCalculationExpression());
        bo.setUpdateFrequency(po.getUpdateFrequency());
        bo.setChartType(po.getChartType());

        bo.setEnableAlert(po.getEnableAlert() == 1);
        bo.setAlertThreshold(po.getAlertThreshold());

        bo.setTags(JSON.parseArray(po.getTags(), String.class));

        return bo;
    }
}
