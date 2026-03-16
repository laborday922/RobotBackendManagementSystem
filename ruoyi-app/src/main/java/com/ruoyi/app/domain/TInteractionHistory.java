package com.ruoyi.app.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 交互历史记录对象 t_interaction_history
 * 
 * @author xiaocai
 * @date 2026-03-13
 */
@JsonInclude(JsonInclude.Include.NON_NULL)//仅序列化非空字段
public class TInteractionHistory extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键，自增ID */
    private Long id;

    /** 任务id */
    private String taskId;

    /** 单次交互唯一标识 */
    private String interactionId;

    /** 交互的机器人ID */
    private Long robotId;

    /** 操作用户ID（如管理员/终端用户） */
    private String userId;

    /** 操作用户名称 */
    private String userName;

    /** 交互类型：0(配送)、1(清洁)、2(巡检)、3(维保)、4(安防) */
    private Long interactionType;

    /** 交互内容（请求/响应数据） */
    private String interactionContent;

    /** 交互发生时间 */
    @JsonFormat(pattern = "yyyy-MM-dd hh:mm:ss")
    private Date interactionTime;

    /** 交互耗时（秒） */
    private Long duration;

    /** 交互评分：0(差)、1(中等)、2(良好) */
    @Excel(name = "交互评分：0(差)、1(中等)、2(良好)")
    private Long rating;

    /** 评价文本（用户/系统评价） */
    @Excel(name = "评价文本", readConverterExp = "用=户/系统评价")
    private String evaluationText;

    /** 交互状态：0(成功)、1(失败)、2(超时) */
    @Excel(name = "交互状态：0(成功)、1(失败)、2(超时)")
    private Long status;

    /** 扩展信息（JSON格式，存储额外字段） */
    @Excel(name = "扩展信息", readConverterExp = "J=SON格式，存储额外字段")
    private String extInfo;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }

    public void setTaskId(String taskId) 
    {
        this.taskId = taskId;
    }

    public String getTaskId() 
    {
        return taskId;
    }

    public void setInteractionId(String interactionId) 
    {
        this.interactionId = interactionId;
    }

    public String getInteractionId() 
    {
        return interactionId;
    }

    public void setRobotId(Long robotId)
    {
        this.robotId = robotId;
    }

    public Long getRobotId()
    {
        return robotId;
    }

    public void setUserId(String userId) 
    {
        this.userId = userId;
    }

    public String getUserId() 
    {
        return userId;
    }

    public void setUserName(String userName) 
    {
        this.userName = userName;
    }

    public String getUserName() 
    {
        return userName;
    }

    public void setInteractionType(Long interactionType) 
    {
        this.interactionType = interactionType;
    }

    public Long getInteractionType() 
    {
        return interactionType;
    }

    public void setInteractionContent(String interactionContent) 
    {
        this.interactionContent = interactionContent;
    }

    public String getInteractionContent() 
    {
        return interactionContent;
    }

    public void setInteractionTime(Date interactionTime) 
    {
        this.interactionTime = interactionTime;
    }

    public Date getInteractionTime() 
    {
        return interactionTime;
    }

    public void setDuration(Long duration) 
    {
        this.duration = duration;
    }

    public Long getDuration() 
    {
        return duration;
    }

    public void setRating(Long rating) 
    {
        this.rating = rating;
    }

    public Long getRating() 
    {
        return rating;
    }

    public void setEvaluationText(String evaluationText) 
    {
        this.evaluationText = evaluationText;
    }

    public String getEvaluationText() 
    {
        return evaluationText;
    }

    public void setStatus(Long status) 
    {
        this.status = status;
    }

    public Long getStatus() 
    {
        return status;
    }

    public void setExtInfo(String extInfo) 
    {
        this.extInfo = extInfo;
    }

    public String getExtInfo() 
    {
        return extInfo;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("taskId", getTaskId())
            .append("interactionId", getInteractionId())
            .append("robotId", getRobotId())
            .append("userId", getUserId())
            .append("userName", getUserName())
            .append("interactionType", getInteractionType())
            .append("interactionContent", getInteractionContent())
            .append("interactionTime", getInteractionTime())
            .append("duration", getDuration())
            .append("rating", getRating())
            .append("evaluationText", getEvaluationText())
            .append("status", getStatus())
            .append("extInfo", getExtInfo())
            .toString();
    }
}
