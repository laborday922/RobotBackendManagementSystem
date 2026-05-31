package com.ruoyi.data.ai.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "tongyi")
public class TongYiConfig {
    // getters and setters
    private String apiKey;
    private String model = "qwen-plus"; // 默认值
    private boolean enableSearch = true;
}
