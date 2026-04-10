package com.ruoyi.data.metric.service.impl;

import com.alibaba.fastjson2.JSON;
import com.ruoyi.common.threadlocal.TenantContext;
import com.ruoyi.common.utils.SecurityUtils;
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

    /**
     * 动态获取当前租户ID（根据用户权限决定是否过滤）
     * 管理员传 null 表示查所有租户，普通用户传自己的租户ID
     */
    private Long getQueryTenantId() {
        Long tenantId = TenantContext.get();
        Long userId = SecurityUtils.getUserId();
        boolean isAdmin = SecurityUtils.isAdmin(userId);
        return isAdmin ? null : tenantId;
    }

    @Override
    public Long create(MetricDefinition bo) {
        MetricDefinitionPo po = convertToPO(bo);
        Long tenantId = getQueryTenantId();   // 使用与查询一致的方法
        // admin 允许 tenantId = null，普通用户不能为 null
        if (tenantId == null && !SecurityUtils.isAdmin(SecurityUtils.getUserId())) {
            throw new RuntimeException("无法获取租户信息，请检查登录状态");
        }
        po.setTenantId(tenantId);
        mapper.insert(po);
        return po.getId();
    }

    @Override
    public MetricDefinition getById(Long id) {
        Long tenantId = getQueryTenantId();
        MetricDefinitionPo po = mapper.selectById(id, tenantId);
        return convertToBO(po);
    }

    @Override
    public List<MetricDefinition> listAll() {
        Long tenantId = getQueryTenantId();
        return mapper.selectAll(tenantId)
                .stream()
                .map(this::convertToBO)
                .collect(Collectors.toList());
    }

    @Override
    public void update(MetricDefinition bo) {
        Long tenantId = getQueryTenantId();
        // 先检查是否存在且属于当前租户（可选，但推荐）
        MetricDefinitionPo existing = mapper.selectById(bo.getId(), tenantId);
        if (existing == null) {
            throw new RuntimeException("指标定义不存在或无权访问");
        }
        MetricDefinitionPo po = convertToPO(bo);
        // 更新时使用传入的 tenantId 作为 WHERE 条件
        mapper.update(po, tenantId);
    }

    @Override
    public void delete(Long id) {
        Long tenantId = getQueryTenantId();
        mapper.delete(id, tenantId);
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
        // tenantId 在外层单独设置
        return po;
    }

    private MetricDefinition convertToBO(MetricDefinitionPo po) {
        if (po == null) {
            return null;
        }
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

    @Override
    public List<String> getFieldsByTableName(String tableName) {
        return mapper.selectColumnsByTableName(tableName);
    }
}