package com.ruoyi.data.dashboard.controller.vo;

import lombok.Data;

import java.util.List;

@Data
public class ServiceOverview {

    /**
     * 在线机器人数量
     */
    private Integer onlineCount;

    /**
     * 机器人总数
     */
    private Integer totalCount;

    /**
     * 可用率（0-100）
     */
    private Double availabilityRate;

    /**
     * 在线状态分布（饼图）
     */
    private List<StatusDistribution> statusDistribution;

    private Integer todayFeedbacks;   // 今日反馈数

    private Integer completedTasks;    // 完成任务总数

}