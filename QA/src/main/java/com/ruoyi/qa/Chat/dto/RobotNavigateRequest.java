package com.ruoyi.qa.Chat.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

public class RobotNavigateRequest
{
    @JsonAlias({"robot_id"})
    private Long robotId;

    @JsonAlias({"robot_point_id", "point_id"})
    private Long robotPointId;

    public Long getRobotId()
    {
        return robotId;
    }

    public void setRobotId(Long robotId)
    {
        this.robotId = robotId;
    }

    public Long getRobotPointId()
    {
        return robotPointId;
    }

    public void setRobotPointId(Long robotPointId)
    {
        this.robotPointId = robotPointId;
    }
}
