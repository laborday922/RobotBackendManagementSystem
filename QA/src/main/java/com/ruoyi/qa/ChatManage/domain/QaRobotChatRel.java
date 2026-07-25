package com.ruoyi.qa.ChatManage.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class QaRobotChatRel extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    @Excel(name = "机器人ID")
    private Long robotId;

    @Excel(name = "问答ID")
    private Long chatId;

    private String robotCode;

    private String robotName;

    private String chatName;

    public Long getRobotId()
    {
        return robotId;
    }

    public void setRobotId(Long robotId)
    {
        this.robotId = robotId;
    }

    public Long getChatId()
    {
        return chatId;
    }

    public void setChatId(Long chatId)
    {
        this.chatId = chatId;
    }

    public String getRobotCode()
    {
        return robotCode;
    }

    public void setRobotCode(String robotCode)
    {
        this.robotCode = robotCode;
    }

    public String getRobotName()
    {
        return robotName;
    }

    public void setRobotName(String robotName)
    {
        this.robotName = robotName;
    }

    public String getChatName()
    {
        return chatName;
    }

    public void setChatName(String chatName)
    {
        this.chatName = chatName;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("robotId", getRobotId())
            .append("chatId", getChatId())
            .append("robotCode", getRobotCode())
            .append("robotName", getRobotName())
            .append("chatName", getChatName())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
