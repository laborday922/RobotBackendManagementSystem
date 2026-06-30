package com.ruoyi.qa.Chat.dto;

import com.alibaba.fastjson2.annotation.JSONField;

import java.util.List;
import java.util.Map;

public class DifyChatMessagesRequest
{
    private Map<String, Object> inputs;
    private String query;

    @JSONField(name = "response_mode")
    private String responseMode;

    @JSONField(name = "conversation_id")
    private String conversationId;

    private String user;
    private List<DifyFile> files;

    public Map<String, Object> getInputs()
    {
        return inputs;
    }

    public void setInputs(Map<String, Object> inputs)
    {
        this.inputs = inputs;
    }

    public String getQuery()
    {
        return query;
    }

    public void setQuery(String query)
    {
        this.query = query;
    }

    public String getResponseMode()
    {
        return responseMode;
    }

    public void setResponseMode(String responseMode)
    {
        this.responseMode = responseMode;
    }

    public String getConversationId()
    {
        return conversationId;
    }

    public void setConversationId(String conversationId)
    {
        this.conversationId = conversationId;
    }

    public String getUser()
    {
        return user;
    }

    public void setUser(String user)
    {
        this.user = user;
    }

    public List<DifyFile> getFiles()
    {
        return files;
    }

    public void setFiles(List<DifyFile> files)
    {
        this.files = files;
    }
}
