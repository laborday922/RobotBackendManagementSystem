package com.ruoyi.qa.Chat.dto;

public class RobotNavigateResponse
{
    private boolean success;
    private String message;
    private Long robotId;
    private Long robotPointId;
    private Long sysPointId;
    private String pointName;
    private String traceId;
    private String mode;
    private boolean completed;
    private Integer progress;
    private String status;

    public boolean isSuccess()
    {
        return success;
    }

    public void setSuccess(boolean success)
    {
        this.success = success;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

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

    public Long getSysPointId()
    {
        return sysPointId;
    }

    public void setSysPointId(Long sysPointId)
    {
        this.sysPointId = sysPointId;
    }

    public String getPointName()
    {
        return pointName;
    }

    public void setPointName(String pointName)
    {
        this.pointName = pointName;
    }

    public String getTraceId()
    {
        return traceId;
    }

    public void setTraceId(String traceId)
    {
        this.traceId = traceId;
    }

    public String getMode()
    {
        return mode;
    }

    public void setMode(String mode)
    {
        this.mode = mode;
    }

    public boolean isCompleted()
    {
        return completed;
    }

    public void setCompleted(boolean completed)
    {
        this.completed = completed;
    }

    public Integer getProgress()
    {
        return progress;
    }

    public void setProgress(Integer progress)
    {
        this.progress = progress;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }
}
