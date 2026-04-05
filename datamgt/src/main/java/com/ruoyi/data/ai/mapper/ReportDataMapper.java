package com.ruoyi.data.ai.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface ReportDataMapper {

    /**
     * 获取任务统计（按状态分组）
     */
    List<Map<String, Object>> getTaskStatistics();

    /**
     * 获取告警统计（按类型、级别分组，支持时间范围）
     */
    List<Map<String, Object>> getWarningStatistics(@Param("startDate") String startDate,
                                                   @Param("endDate") String endDate);

    /**
     * 获取最新清洗任务中的所有交互文本
     */
    List<String> getLatestCleanedInteractions();

    /**
     * 根据时间范围获取交互文本（使用 clean_time 过滤）
     */
    List<String> getCleanedInteractionsByTime(@Param("startDate") String startDate,
                                              @Param("endDate") String endDate);
}