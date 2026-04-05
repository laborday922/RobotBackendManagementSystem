package com.ruoyi.data.ai.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Component
@ConfigurationProperties(prefix = "tongyi")
public class TongYiConfig {
    // getters and setters
    private String apiKey;
    private String model = "qwen-plus"; // 默认值
    private boolean enableSearch = true;

    public void setApiKey(String apiKey) { this.apiKey = apiKey; }

    public void setModel(String model) { this.model = model; }

    public void setEnableSearch(boolean enableSearch) { this.enableSearch = enableSearch; }
}