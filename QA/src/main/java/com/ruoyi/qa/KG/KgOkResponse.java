package com.ruoyi.qa.KG;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class KgOkResponse
{
    private Boolean ok;
    private String message;
    private Map<String, Object> data;

    public Boolean getOk()
    {
        return ok;
    }

    public void setOk(Boolean ok)
    {
        this.ok = ok;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public Map<String, Object> getData()
    {
        return data;
    }

    public void setData(Map<String, Object> data)
    {
        this.data = data;
    }
}
