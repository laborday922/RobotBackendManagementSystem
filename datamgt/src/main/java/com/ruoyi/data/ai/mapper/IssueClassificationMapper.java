package com.ruoyi.data.ai.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface IssueClassificationMapper {

    // 获取原始交互数据（给 AI 用）
    List<Map<String, Object>> selectRawInteractions(
            @Param("startTime") Date startTime,
            @Param("endTime") Date endTime,
            @Param("tenantId") Long tenantId
    );

    // 获取某分类的原始数据（用于趋势）
    List<Map<String, Object>> selectRawByCategory(
            @Param("category") Integer category,
            @Param("startTime") Date startTime,
            @Param("endTime") Date endTime,
            @Param("tenantId") Long tenantId
    );

}