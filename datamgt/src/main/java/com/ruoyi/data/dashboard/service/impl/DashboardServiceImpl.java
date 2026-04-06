package com.ruoyi.data.dashboard.service.impl;

import com.ruoyi.common.threadlocal.TenantContext;
import com.ruoyi.common.utils.SecurityUtils;
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

    /**
     * 动态获取当前租户ID（根据用户权限决定是否过滤）
     * 管理员传 null 表示查所有租户，普通用户传自己的租户ID
     */
    private Long getQueryTenantId() {
        Long tenantId = TenantContext.get();
        Long userId = SecurityUtils.getUserId();
        boolean isAdmin = SecurityUtils.isAdmin(userId);
        return isAdmin ? null : tenantId;
    }

    @Override
    public List<WordCloudItem> getFeedbackWordCloud(Date startTime,
                                                    Date endTime,
                                                    String feedbackType) {

        Integer rating = convertRating(feedbackType);
        Long tenantId = getQueryTenantId();

        // 1️⃣ 查数据库（传入 tenantId）
        List<InteractionEvaluationPo> records =
                dashboardMapper.selectEvaluationTexts(tenantId, startTime, endTime, rating);

        // 2️⃣ 提取文本
        List<String> texts = records.stream()
                .map(InteractionEvaluationPo::getEvaluationText)
                .filter(t -> t != null && !t.isEmpty())
                .toList();

        // 3️⃣ 调 AI 服务（AI服务内部可能也需要租户隔离，暂不改动）
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
        Long tenantId = getQueryTenantId();
        List<RobotPositionLatestPo> pos =
                dashboardMapper.selectRobotLatestPositions(tenantId);

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

    @Override
    public ServiceOverview getServiceOverview() {
        Long tenantId = getQueryTenantId();
        Integer total = dashboardMapper.selectRobotTotalCount(tenantId);
        Integer online = dashboardMapper.selectOnlineRobotCount(tenantId);
        List<RobotStatusCountPo> list = dashboardMapper.selectStatusDistribution(tenantId);

        double rate = total == 0 ? 0 : (double) online / total * 100;

        List<StatusDistribution> distribution = list.stream()
                .map(e -> {
                    StatusDistribution vo = new StatusDistribution();
                    vo.setName(convertStatus(e.getStatus()));
                    vo.setValue(e.getCount());
                    return vo;
                }).toList();

        Integer todayFeedbacks = dashboardMapper.selectTodayFeedbackCount(tenantId);
        Integer completedTasks = dashboardMapper.selectCompletedTaskCount(tenantId);

        ServiceOverview vo = new ServiceOverview();
        vo.setTotalCount(total);
        vo.setOnlineCount(online);
        vo.setAvailabilityRate(Math.round(rate * 100.0) / 100.0);
        vo.setStatusDistribution(distribution);
        vo.setTodayFeedbacks(todayFeedbacks);
        vo.setCompletedTasks(completedTasks);
        return vo;
    }

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

    @Override
    public TaskExecutionListResponse getTaskExecutions(Date startTime,
                                                       Date endTime,
                                                       Integer limit,
                                                       Integer offset) {
        Long tenantId = getQueryTenantId();
        List<TaskExecutionPo> list =
                dashboardMapper.selectTaskExecutions(tenantId, startTime, endTime, limit, offset);

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

    @Override
    public List<RobotGroup> getRobotGroups() {
        Long tenantId = getQueryTenantId();
        List<RobotGroupPo> list = dashboardMapper.selectAllRobotGroups(tenantId);

        return list.stream()
                .map(po -> {
                    RobotGroup vo = new RobotGroup();
                    vo.setId(po.getId());
                    vo.setName(po.getName());
                    vo.setDescription(po.getDescription());
                    vo.setOnlineCount(po.getOnlineCount() != null ? po.getOnlineCount() : 0);
                    vo.setTotalCount(po.getTotalCount() != null ? po.getTotalCount() : 0);
                    return vo;
                })
                .toList();
    }

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

    private String formatTime(String time, String granularity) {
        if ("week".equals(granularity)) {
            return "第" + time.substring(4) + "周";
        }
        return time;
    }

    @Override
    public TimeSeriesData getAnomalyTrend(String granularity, String timeRange) {
        Date[] range = parseTimeRange(timeRange);
        Date startTime = range[0];
        Date endTime = range[1];
        Long tenantId = getQueryTenantId();

        List<AnomalyTrendPo> list =
                dashboardMapper.selectAnomalyTrend(tenantId, startTime, endTime, granularity);

        Map<String, Integer> dataMap = new HashMap<>();
        for (AnomalyTrendPo po : list) {
            dataMap.put(po.getTime(), po.getCount());
        }

        List<String> xAxis = new ArrayList<>();
        List<Integer> series = new ArrayList<>();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startTime);

        SimpleDateFormat sdf;
        switch (granularity) {
            case "hour":
                sdf = new SimpleDateFormat("yyyy-MM-dd HH:00");
                break;
            case "month":
                sdf = new SimpleDateFormat("yyyy-MM");
                break;
            case "week":
                sdf = new SimpleDateFormat("YYYY-'W'ww");
                break;
            default:
                sdf = new SimpleDateFormat("yyyy-MM-dd");
        }

        while (!calendar.getTime().after(endTime)) {
            String key = sdf.format(calendar.getTime());
            xAxis.add(key);
            series.add(dataMap.getOrDefault(key, 0));
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