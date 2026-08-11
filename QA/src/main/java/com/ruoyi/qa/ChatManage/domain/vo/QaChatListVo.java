package com.ruoyi.qa.ChatManage.domain.vo;

import java.util.Date;

public class QaChatListVo
{
    private Long id;

    private String chatName;

    private String chatDesc;

    private String chatType;

    private Boolean hasApiKey;

    private String apiKeyMasked;

    private Date updateTime;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getChatName()
    {
        return chatName;
    }

    public void setChatName(String chatName)
    {
        this.chatName = chatName;
    }

    public String getChatDesc()
    {
        return chatDesc;
    }

    public void setChatDesc(String chatDesc)
    {
        this.chatDesc = chatDesc;
    }

    public String getChatType()
    {
        return chatType;
    }

    public void setChatType(String chatType)
    {
        this.chatType = chatType;
    }

    public Boolean getHasApiKey()
    {
        return hasApiKey;
    }

    public void setHasApiKey(Boolean hasApiKey)
    {
        this.hasApiKey = hasApiKey;
    }

    public String getApiKeyMasked()
    {
        return apiKeyMasked;
    }

    public void setApiKeyMasked(String apiKeyMasked)
    {
        this.apiKeyMasked = apiKeyMasked;
    }

    public Date getUpdateTime()
    {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime)
    {
        this.updateTime = updateTime;
    }
}
