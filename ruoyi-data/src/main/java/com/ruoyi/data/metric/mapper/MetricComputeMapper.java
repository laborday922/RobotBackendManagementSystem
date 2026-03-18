package com.ruoyi.data.metric.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface MetricComputeMapper {

    Double executeMetricSQL(@Param("sql") String sql);

    List<Map<String,Object>> executeQuery(@Param("sql") String sql);

}