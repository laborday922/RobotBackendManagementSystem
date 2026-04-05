package com.ruoyi.data.dashboard.service.impl;

import com.ruoyi.data.ai.service.AiAnalysisService;
import com.ruoyi.data.dashboard.controller.vo.*;
import com.ruoyi.data.dashboard.mapper.DashboardMapper;
import com.ruoyi.data.dashboard.mapper.po.*;
import com.ruoyi.data.dashboard.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private DashboardMapper dashboardMapper;

    @Autowired
    private AiAnalysisService aiAnalysisService;

    @Override
    public List<WordCloudItem> getFeedbackWordCloud(Date startTime,
                                                    Date endTime,
                                                    String feedbackType) {

        Integer rating = convertRating(feedbackType);

        // 1️⃣ 查数据库
        List<InteractionEvaluationPo> records =
                dashboardMapper.selectEvaluationTexts(startTime, endTime, rating);

        // 2️⃣ 提取文本
        List<String> texts = records.stream()
                .map(InteractionEvaluationPo::getEvaluationText)
                .filter(t -> t != null && !t.isEmpty())
                .toList();

        // 3️⃣ 调 AI 服务
        return aiAnalysisService.generateWordCloud(texts);
    }

    private Integer convertRating(String type) {

        if (type == null) {
            return null;
        }

        switch (type) {
            case "好评":
                return 2;
            case "差评":
                return 0;
            case "建议":
                return 1;
        }

        return null;
    }

    @Override
    public List<RobotGeoInfo> getRobotGeoDistribution() {

        List<RobotPositionLatestPo> pos =
                dashboardMapper.selectRobotLatestPositions();

        return pos.stream()
                .map(p -> {

                    RobotGeoInfo vo = new RobotGeoInfo();

                    vo.setRobotId(p.getRobotId());
                    vo.setLocationArea(p.getLocationArea());
                    vo.setSpecificLocation(p.getSpecificLocation());
                    vo.setCoordinateX(p.getCoordinateX());
                    vo.setCoordinateY(p.getCoordinateY());
                    vo.setMoveSpeed(p.getMoveSpeed());
                    vo.setStatusDesc(p.getStatusDesc());

                    return vo;

                })
                .toList();
    }

    //机器人状态概览
    @Override
    public ServiceOverview getServiceOverview() {

        Integer total = dashboardMapper.selectRobotTotalCount();
        Integer online = dashboardMapper.selectOnlineRobotCount();
        List<RobotStatusCountPo> list = dashboardMapper.selectStatusDistribution();

        double rate = total == 0 ? 0 : (double) online / total * 100;

        List<StatusDistribution> distribution = list.stream()
                .map(e -> {
                    StatusDistribution vo = new StatusDistribution();
                    vo.setName(convertStatus(e.getStatus()));
                    vo.setValue(e.getCount());
                    return vo;
                }).toList();

        // 新增：获取今日反馈数和完成任务数
        Integer todayFeedbacks = dashboardMapper.selectTodayFeedbackCount();
        Integer completedTasks = dashboardMapper.selectCompletedTaskCount();

        ServiceOverview vo = new ServiceOverview();
        vo.setTotalCount(total);
        vo.setOnlineCount(online);
        vo.setAvailabilityRate(Math.round(rate * 100.0) / 100.0);
        vo.setStatusDistribution(distribution);
        vo.setTodayFeedbacks(todayFeedbacks);      // 设置今日反馈
        vo.setCompletedTasks(completedTasks);      // 设置完成任务

        return vo;
    }

    //状态转换
    private String convertStatus(Integer status) {

        switch (status) {
            case 0:
                return "离线";
            case 1:
                return "在线";
            case 2:
                return "待激活";
            default:
                return "未知";
        }
    }

    //机器人任务执行情况
    @Override
    public TaskExecutionListResponse getTaskExecutions(Date startTime,
                                                       Date endTime,
                                                       Integer limit,
                                                       Integer offset) {

        List<TaskExecutionPo> list =
                dashboardMapper.selectTaskExecutions(startTime, endTime, limit, offset);

        List<TaskExecutionItem> running = new ArrayList<>();
        List<TaskExecutionItem> pending = new ArrayList<>();

        for (TaskExecutionPo po : list) {

            TaskExecutionItem vo = convertToVO(po);

            if (po.getStatus() == 1) {
                running.add(vo);
            } else if (po.getStatus() == 0) {
                pending.add(vo);
            }
        }

        TaskExecutionListResponse res = new TaskExecutionListResponse();

        res.setRunningTasks(running);
        res.setPendingTasks(pending);
        res.setTotal(list.size());

        return res;
    }

    private TaskExecutionItem convertToVO(TaskExecutionPo po) {

        TaskExecutionItem vo = new TaskExecutionItem();

        vo.setId(po.getId());
        vo.setName(po.getName());
        vo.setRobotId(po.getRobotId());
        vo.setStatus(po.getStatus());
        vo.setStatusDesc(convertTaskStatus(po.getStatus()));
        vo.setScheduledTime(po.getScheduledTime());
        vo.setPriority(po.getPriority());

        return vo;
    }

    private String convertTaskStatus(Integer status) {

        switch (status) {
            case 0:
                return "待执行";
            case 1:
                return "执行中";
            case 2:
                return "已完成";
            case 3:
                return "失败";
            default:
                return "未知";
        }
    }

    //机器人分组
    @Override
    public List<RobotGroup> getRobotGroups() {

        List<RobotGroupPo> list = dashboardMapper.selectAllRobotGroups();

        return list.stream()
                .map(po -> {
                    RobotGroup vo = new RobotGroup();
                    vo.setId(po.getId());
                    vo.setName(po.getName());
                    vo.setDescription(po.getDescription());
                    // 新增：赋值在线数量和总数
                    vo.setOnlineCount(po.getOnlineCount() != null ? po.getOnlineCount() : 0);
                    vo.setTotalCount(po.getTotalCount() != null ? po.getTotalCount() : 0);
                    return vo;
                })
                .toList();
    }

    //时间范围解析
    private Date[] parseTimeRange(String timeRange) {

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start;

        switch (timeRange) {
            case "last_7_days":
                start = now.minusDays(7);
                break;
            case "last_30_days":
                start = now.minusDays(30);
                break;
            case "last_24_hours":
                start = now.minusHours(24);
                break;
            default:
                start = now.minusDays(7);
        }

        return new Date[]{
                Timestamp.valueOf(start),
                Timestamp.valueOf(now)
        };
    }

    //时间格式优化
    private String formatTime(String time, String granularity) {

        if ("week".equals(granularity)) {
            return "第" + time.substring(4) + "周";
        }

        return time;
    }

    //异常情况趋势
    @Override
    public TimeSeriesData getAnomalyTrend(String granularity, String timeRange) {

        Date[] range = parseTimeRange(timeRange);
        Date startTime = range[0];
        Date endTime = range[1];

        List<AnomalyTrendPo> list =
                dashboardMapper.selectAnomalyTrend(
                        startTime,
                        endTime,
                        granularity
                );

        // ✅ 1. 转 Map（key=时间字符串）
        Map<String, Integer> dataMap = new HashMap<>();
        for (AnomalyTrendPo po : list) {
            dataMap.put(po.getTime(), po.getCount());
        }

        List<String> xAxis = new ArrayList<>();
        List<Integer> series = new ArrayList<>();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startTime);

        // ✅ 2. 根据粒度定义时间格式
        SimpleDateFormat sdf;
        switch (granularity) {
            case "hour":
                sdf = new SimpleDateFormat("yyyy-MM-dd HH:00");
                break;
            case "month":
                sdf = new SimpleDateFormat("yyyy-MM");
                break;
            case "week":
                sdf = new SimpleDateFormat("YYYY-'W'ww"); // ISO 周
                break;
            default: // day
                sdf = new SimpleDateFormat("yyyy-MM-dd");
        }

        // ✅ 3. 补全时间轴
        while (!calendar.getTime().after(endTime)) {

            String key = sdf.format(calendar.getTime());

            xAxis.add(key);
            series.add(dataMap.getOrDefault(key, 0)); // 👈 补0核心

            // ✅ 4. 时间递增
            switch (granularity) {
                case "hour":
                    calendar.add(Calendar.HOUR_OF_DAY, 1);
                    break;
                case "week":
                    calendar.add(Calendar.WEEK_OF_YEAR, 1);
                    break;
                case "month":
                    calendar.add(Calendar.MONTH, 1);
                    break;
                default:
                    calendar.add(Calendar.DAY_OF_MONTH, 1);
            }
        }

        TimeSeriesData data = new TimeSeriesData();
        data.setXAxis(xAxis);
        data.setSeries(series);

        return data;
    }
}