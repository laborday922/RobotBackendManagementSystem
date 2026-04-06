package com.ruoyi.data.dashboard.mapper;

import com.ruoyi.data.dashboard.mapper.po.*;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface DashboardMapper {

    //获取用户评价文本
    List<InteractionEvaluationPo> selectEvaluationTexts(
            @Param("tenantId") Long tenantId,
            @Param("startTime") Date startTime,
            @Param("endTime") Date endTime,
            @Param("rating") Integer rating
    );

    //获取机器人当前位置
    List<RobotPositionLatestPo> selectRobotLatestPositions(
            @Param("tenantId") Long tenantId
    );

    //获取机器人总数
    Integer selectRobotTotalCount(
            @Param("tenantId") Long tenantId
    );

    //获取机器人在线数
    Integer selectOnlineRobotCount(
            @Param("tenantId") Long tenantId
    );

    //获取机器人状态总数
    List<RobotStatusCountPo> selectStatusDistribution(
            @Param("tenantId") Long tenantId
    );

    //获取任务执行情况
    List<TaskExecutionPo> selectTaskExecutions(
            @Param("tenantId") Long tenantId,
            @Param("startTime") Date startTime,
            @Param("endTime") Date endTime,
            @Param("limit") Integer limit,
            @Param("offset") Integer offset
    );

    //获取所有分组
    List<RobotGroupPo> selectAllRobotGroups(
            @Param("tenantId") Long tenantId
    );

    //获取异常趋势数据
    List<AnomalyTrendPo> selectAnomalyTrend(
            @Param("tenantId") Long tenantId,
            @Param("startTime") Date startTime,
            @Param("endTime") Date endTime,
            @Param("granularity") String granularity
    );

    //获取今日反馈总数
    Integer selectTodayFeedbackCount(
            @Param("tenantId") Long tenantId
    );

    //获取任务完成总数
    Integer selectCompletedTaskCount(
            @Param("tenantId") Long tenantId
    );
}