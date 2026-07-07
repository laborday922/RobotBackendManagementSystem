package com.ruoyi.qa.Dify.dto;

import com.alibaba.fastjson2.annotation.JSONField;

public class DifyDocument
{
    @JSONField(name = "id")
    private String id;

    @JSONField(name = "name")
    private String name;

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }
}

