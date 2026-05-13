package com.ruoyi.data.metric.service.impl;

import com.ruoyi.common.threadlocal.TenantContext;
import com.ruoyi.data.metric.chartStrategy.ChartDataStrategy;
import com.ruoyi.data.metric.mapper.MetricComputeMapper;
import com.ruoyi.data.metric.mapper.MetricDefinitionMapper;
import com.ruoyi.data.metric.mapper.po.MetricDefinitionPo;
import com.ruoyi.data.metric.service.MetricComputeService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

import static com.ruoyi.common.utils.SecurityUtils.isAdmin;

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

        Long tenantId = TenantContext.get();

        // ✅ 1. 查询指标（带租户）
        // 如果是管理员，清空租户限制
        if (isAdmin()) {
            tenantId = null;
        }
        MetricDefinitionPo metric = definitionMapper.selectById(metricId, tenantId);

        if (metric == null) {
            throw new RuntimeException("指标不存在或无权限访问");
        }

        // ✅ 2. 获取原始 SQL
        String sql = metric.getCalculationExpression();

        // 注入 tenant 条件
        sql = appendTenantCondition(sql, tenantId);

        // ✅ 3. 执行 SQL
        List<Map<String, Object>> result = computeMapper.executeQuery(sql);

        if (result != null && !result.isEmpty()) {
            Map<String, Object> firstRow = result.get(0);

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

        Long tenantId = TenantContext.get();

        // 1. 查询指标定义（带租户隔离）
        // 管理员可绕过租户限制，清空 tenantId 以便查询所有租户数据
        if (isAdmin()) {
            tenantId = null;
        }
        MetricDefinitionPo metric = definitionMapper.selectById(metricId, tenantId);

        if (metric == null) {
            throw new RuntimeException("指标不存在或无权限访问");
        }

        // 2. 获取指标的 SQL 计算表达式
        String sql = metric.getCalculationExpression();

        // 向 SQL 中追加租户过滤条件（如 where tenant_id = ?）
        sql = appendTenantCondition(sql, tenantId);

        // 3. 执行 SQL，获取原始查询结果
        List<Map<String, Object>> data = computeMapper.executeQuery(sql);

        // 4. 根据图表类型（柱状图、饼图、折线图等）获取对应的数据构建策略
        ChartDataStrategy strategy = chartStrategyMap.get(metric.getChartType());

        // 5. 将原始数据转换为前端需要的图表数据格式
        return strategy.buildChartData(data);
    }

    /**
     * 多租户 SQL 注入工具
     * 智能插入 tenant_id 条件到正确位置（在 GROUP BY / ORDER BY / LIMIT 之前）
     */
    private String appendTenantCondition(String sql, Long tenantId) {
        if (tenantId == null) {
            return sql;
        }

        // 注意：条件前后都加空格，避免粘连
        String condition = " tenant_id = " + tenantId + " ";
        String lowerSql = sql.toLowerCase();

        int groupByIndex = lowerSql.indexOf("group by");
        int orderByIndex = lowerSql.indexOf("order by");
        int limitIndex = lowerSql.indexOf("limit");

        int insertPos = sql.length();
        if (groupByIndex != -1) insertPos = Math.min(insertPos, groupByIndex);
        if (orderByIndex != -1) insertPos = Math.min(insertPos, orderByIndex);
        if (limitIndex != -1) insertPos = Math.min(insertPos, limitIndex);

        int whereIndex = lowerSql.indexOf("where");
        if (whereIndex != -1 && whereIndex < insertPos) {
            // 已有 WHERE，在插入点前追加 AND，注意 AND 前后加空格
            return sql.substring(0, insertPos) + " AND " + condition + sql.substring(insertPos);
        } else {
            // 无 WHERE，插入 WHERE，注意 WHERE 前后加空格
            return sql.substring(0, insertPos) + " WHERE " + condition + sql.substring(insertPos);
        }
    }
}