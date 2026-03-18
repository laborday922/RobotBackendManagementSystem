package com.ruoyi.data.dashboard.mapper;

import com.ruoyi.data.dashboard.mapper.po.InteractionEvaluationPo;
import com.ruoyi.data.dashboard.mapper.po.RobotPositionLatestPo;
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

}