package com.ruoyi.data.dashboard.service;

import com.ruoyi.data.dashboard.controller.vo.RobotGeoInfo;
import com.ruoyi.data.dashboard.controller.vo.WordCloudItem;

import java.util.Date;
import java.util.List;

public interface DashboardService {

    List<WordCloudItem> getFeedbackWordCloud(
            Date startTime,
            Date endTime,
            String feedbackType
    );

    List<RobotGeoInfo> getRobotGeoDistribution();

}