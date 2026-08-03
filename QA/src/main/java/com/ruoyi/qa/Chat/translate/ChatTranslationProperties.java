package com.ruoyi.qa.Chat.translate;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "qa.chat.translation")
public class ChatTranslationProperties
{
    private boolean enabled = false;
    private String baseUrl;
    private String apiKey;
    private String model;
    private int connectTimeoutSeconds = 10;
    private int requestTimeoutSeconds = 60;
    private int streamFlushThreshold = 80;

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

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

    public String getModel()
    {
        return model;
    }

    public void setModel(String model)
    {
        this.model = model;
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

    public int getStreamFlushThreshold()
    {
        return streamFlushThreshold;
    }

    public void setStreamFlushThreshold(int streamFlushThreshold)
    {
        this.streamFlushThreshold = streamFlushThreshold;
    }
}
