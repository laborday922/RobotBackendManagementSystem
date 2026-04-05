package com.ruoyi.data.metric.mapper;

import com.ruoyi.data.metric.mapper.po.MetricDefinitionPo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MetricDefinitionMapper {

    int insert(MetricDefinitionPo po);

    MetricDefinitionPo selectById(Long id);

    List<MetricDefinitionPo> selectAll();

    int update(MetricDefinitionPo po);

    int delete(Long id);

    //获取数据源字段
    List<String> selectColumnsByTableName(@Param("tableName") String tableName);

}
