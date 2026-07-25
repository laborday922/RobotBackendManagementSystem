package com.ruoyi.qa.ChatManage.domain.vo;

public class QaChatDetailVo
{
    private Long id;

    private String chatName;

    private String chatDesc;

    private String difyApiKey;

    private Boolean hasDifyApiKey;

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

    public String getDifyApiKey()
    {
        return difyApiKey;
    }

    public void setDifyApiKey(String difyApiKey)
    {
        this.difyApiKey = difyApiKey;
    }

    public Boolean getHasDifyApiKey()
    {
        return hasDifyApiKey;
    }

    public void setHasDifyApiKey(Boolean hasDifyApiKey)
    {
        this.hasDifyApiKey = hasDifyApiKey;
    }
}
