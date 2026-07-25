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

    @Excel(name = "Dify应用Key")
    private String difyApiKey;

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

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("chatName", getChatName())
            .append("chatDesc", getChatDesc())
            .append("difyApiKey", getDifyApiKey())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
