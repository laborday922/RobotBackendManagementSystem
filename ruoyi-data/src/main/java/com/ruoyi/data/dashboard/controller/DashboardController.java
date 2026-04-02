package com.ruoyi.data.dashboard.controller;

import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.data.dashboard.controller.vo.*;
import com.ruoyi.data.dashboard.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    //获取用户反馈词云数据
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

    //获取机器人位置
    @GetMapping("/service/robots/geo")
    public AjaxResult getRobotGeoDistribution() {

        List<RobotGeoInfo> data =
                dashboardService.getRobotGeoDistribution();

        return AjaxResult.success(data);
    }

    //获取机器人状态
    @GetMapping("/service/overview")
    public AjaxResult getServiceOverview() {

        ServiceOverview data =
                dashboardService.getServiceOverview();

        return AjaxResult.success(data);
    }

    //获取任务执行状态
    @GetMapping("/tasks/executions")
    public AjaxResult getTaskExecutions(
            @RequestParam(required = false) Date start_time,
            @RequestParam(required = false) Date end_time,
            @RequestParam(defaultValue = "20") Integer limit,
            @RequestParam(defaultValue = "0") Integer offset) {

        TaskExecutionListResponse data =
                dashboardService.getTaskExecutions(
                        start_time,
                        end_time,
                        limit,
                        offset
                );

        return AjaxResult.success(data);
    }

    //获取机器人分组
    @GetMapping("/robot/groups")
    public AjaxResult getRobotGroups() {

        List<RobotGroup> data = dashboardService.getRobotGroups();

        return AjaxResult.success(data);
    }

    //获取异常趋势统计
    @GetMapping("/anomaly/trend")
    public AjaxResult getAnomalyTrend(
            @RequestParam String granularity,
            @RequestParam String time_range) {

        TimeSeriesData data =
                dashboardService.getAnomalyTrend(granularity, time_range);

        return AjaxResult.success(data);
    }
}
