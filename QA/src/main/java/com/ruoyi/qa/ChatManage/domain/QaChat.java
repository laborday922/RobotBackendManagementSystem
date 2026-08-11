package com.ruoyi.qa.ChatManage.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class QaChat extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long id;

    @Excel(name = "问答名称")
    private String chatName;

    @Excel(name = "问答描述")
    private String chatDesc;

    @Excel(name = "对话类型")
    private String chatType;

    @Excel(name = "API Key")
    private String apiKey;

    @Excel(name = "接口地址")
    private String baseUrl;

    @Excel(name = "模型名称")
    private String modelName;

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

    public String getApiKey()
    {
        return apiKey;
    }

    public void setApiKey(String apiKey)
    {
        this.apiKey = apiKey;
    }

    public String getBaseUrl()
    {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl)
    {
        this.baseUrl = baseUrl;
    }

    public String getModelName()
    {
        return modelName;
    }

    public void setModelName(String modelName)
    {
        this.modelName = modelName;
    }

    public boolean isDify()
    {
        return "dify".equalsIgnoreCase(chatType);
    }

    public boolean isOpenai()
    {
        return "openai".equalsIgnoreCase(chatType);
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("chatName", getChatName())
            .append("chatDesc", getChatDesc())
            .append("chatType", getChatType())
            .append("apiKey", getApiKey())
            .append("baseUrl", getBaseUrl())
            .append("modelName", getModelName())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
