package com.ruoyi.qa.Chat.dify;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "dify")
public class DifyProperties
{
    private String baseUrl = "http://119.91.158.102:8048/v1";
    private String apiKey = "app-EdNaeoq8ej55IDR7LGo6Clz1";
    private int connectTimeoutSeconds = 10;
    private int requestTimeoutSeconds = 300;

    public String getBaseUrl()
    {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl)
    {
        this.baseUrl = baseUrl;
    }

    public String getApiKey()
    {
        return apiKey;
    }

    public void setApiKey(String apiKey)
    {
        this.apiKey = apiKey;
    }

    public int getConnectTimeoutSeconds()
    {
        return connectTimeoutSeconds;
    }

    public void setConnectTimeoutSeconds(int connectTimeoutSeconds)
    {
        this.connectTimeoutSeconds = connectTimeoutSeconds;
    }

    public int getRequestTimeoutSeconds()
    {
        return requestTimeoutSeconds;
    }

    public void setRequestTimeoutSeconds(int requestTimeoutSeconds)
    {
        this.requestTimeoutSeconds = requestTimeoutSeconds;
    }
}
