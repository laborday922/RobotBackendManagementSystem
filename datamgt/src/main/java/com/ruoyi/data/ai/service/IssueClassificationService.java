package com.ruoyi.data.ai.service;

import com.ruoyi.data.ai.domain.bo.CategoryCount;
import com.ruoyi.data.ai.domain.bo.TimeSeriesData;

import java.util.List;

public interface IssueClassificationService {

    List<CategoryCount> getIssueDistribution(String timeRange);

    TimeSeriesData getIssueTrend(String category, String granularity, String timeRange);
}