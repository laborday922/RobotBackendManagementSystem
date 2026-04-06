package com.ruoyi.data.metric.mapper;

import com.ruoyi.data.metric.mapper.po.MetricDefinitionPo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MetricDefinitionMapper {

    int insert(MetricDefinitionPo po);

    MetricDefinitionPo selectById(@Param("id") Long id,
                                  @Param("tenantId") Long tenantId);

    List<MetricDefinitionPo> selectAll(@Param("tenantId") Long tenantId);

    int update(@Param("metric") MetricDefinitionPo metric,
               @Param("tenantId") Long tenantId);

    int delete(@Param("id") Long id,
               @Param("tenantId") Long tenantId);

    //获取数据源字段
    List<String> selectColumnsByTableName(@Param("tableName") String tableName);

}
