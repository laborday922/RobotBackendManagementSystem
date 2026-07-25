package com.ruoyi.qa.Dify;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "dify")
public class DifyChatProperties
{
    private String baseUrl;
    private String apiKey;
    private String datasetId;
    private String datasetApiKey;
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

    public String getDatasetId()
    {
        return datasetId;
    }

    public void setDatasetId(String datasetId)
    {
        this.datasetId = datasetId;
    }

    public String getDatasetApiKey()
    {
        return datasetApiKey;
    }

    public void setDatasetApiKey(String datasetApiKey)
    {
        this.datasetApiKey = datasetApiKey;
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
