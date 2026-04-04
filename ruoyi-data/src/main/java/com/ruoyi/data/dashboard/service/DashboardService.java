package com.ruoyi.data.dashboard.service;

import com.ruoyi.data.dashboard.controller.vo.*;

import java.util.Date;
import java.util.List;

public interface DashboardService {

    List<WordCloudItem> getFeedbackWordCloud(
            Date startTime,
            Date endTime,
            String feedbackType
    );

    List<RobotGeoInfo> getRobotGeoDistribution();

    ServiceOverview getServiceOverview();

    TaskExecutionListResponse getTaskExecutions(Date startTime,
                                                Date endTime,
                                                Integer limit,
                                                Integer offset);

    List<RobotGroup> getRobotGroups();

    TimeSeriesData getAnomalyTrend(String granularity,
                                   String timeRange);
}