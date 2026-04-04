package com.ruoyi.data.dashboard.mapper;

import com.ruoyi.data.dashboard.mapper.po.*;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface DashboardMapper {

    List<InteractionEvaluationPo> selectEvaluationTexts(
            @Param("startTime") Date startTime,
            @Param("endTime") Date endTime,
            @Param("rating") Integer rating
    );

    List<RobotPositionLatestPo> selectRobotLatestPositions();

    Integer selectRobotTotalCount();//机器人总数

    Integer selectOnlineRobotCount();//机器人在线总数

    List<RobotStatusCountPo> selectStatusDistribution();//机器人状态列表

    //任务执行情况
    List<TaskExecutionPo> selectTaskExecutions(
            @Param("startTime") Date startTime,
            @Param("endTime") Date endTime,
            @Param("limit") Integer limit,
            @Param("offset") Integer offset
    );

    List<RobotGroupPo> selectAllRobotGroups();//机器人分组列表

    //异常趋势统计
    List<AnomalyTrendPo> selectAnomalyTrend(
            @Param("startTime") Date startTime,
            @Param("endTime") Date endTime,
            @Param("granularity") String granularity
    );

    Integer selectTodayFeedbackCount();

    Integer selectCompletedTaskCount();

}