package com.ruoyi.qa.Chat.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

public class RobotChatRequest
{
    @JsonAlias({"robot_id"})
    private String robotId;

    @JsonAlias({"conversation_id", "coversationId"})
    private String conversationId;

    @JsonAlias({"message", "content"})
    private String query;

    public String getRobotId()
    {
        return robotId;
    }

    public void setRobotId(String robotId)
    {
        this.robotId = robotId;
    }

    public String getConversationId()
    {
        return conversationId;
    }

    public void setConversationId(String conversationId)
    {
        this.conversationId = conversationId;
    }

    public String getQuery()
    {
        return query;
    }

    public void setQuery(String query)
    {
        this.query = query;
    }
}
