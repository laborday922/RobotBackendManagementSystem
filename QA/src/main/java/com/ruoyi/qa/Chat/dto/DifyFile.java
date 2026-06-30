package com.ruoyi.qa.Chat.dto;

import com.alibaba.fastjson2.annotation.JSONField;

public class DifyFile
{
    private String type;

    @JSONField(name = "transfer_method")
    private String transferMethod;

    private String url;

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public String getTransferMethod()
    {
        return transferMethod;
    }

    public void setTransferMethod(String transferMethod)
    {
        this.transferMethod = transferMethod;
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }
}
