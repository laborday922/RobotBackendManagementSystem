package com.ruoyi.data.dashboard.controller;

import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.data.dashboard.controller.vo.RobotGeoInfo;
import com.ruoyi.data.dashboard.controller.vo.WordCloudItem;
import com.ruoyi.data.dashboard.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/feedback/wordcloud")
    public AjaxResult getFeedbackWordCloud(
            @RequestParam(required = false) Date start_time,
            @RequestParam(required = false) Date end_time,
            @RequestParam(required = false) String feedback_type) {

        List<WordCloudItem> data =
                dashboardService.getFeedbackWordCloud(
                        start_time,
                        end_time,
                        feedback_type
                );

        return AjaxResult.success(data);
    }

    @GetMapping("/service/robots/geo")
    public AjaxResult getRobotGeoDistribution() {

        List<RobotGeoInfo> data =
                dashboardService.getRobotGeoDistribution();

        return AjaxResult.success(data);
    }
}
