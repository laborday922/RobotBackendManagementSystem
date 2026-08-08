package com.ruoyi.data.ai.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "siliconflow")
public class SiliconFlowConfig {
    private String apiKey;
    private String baseUrl = "https://api.siliconflow.cn";
    private String model = "Qwen/Qwen2.5-72B-Instruct";
}
