package com.ruoyi.data.ai.service;

import com.ruoyi.data.ai.config.SiliconFlowConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SiliconFlowService {
    private static final Logger log = LoggerFactory.getLogger(SiliconFlowService.class);

    @Autowired
    private SiliconFlowConfig config;

    private final RestTemplate restTemplate = new RestTemplate();

    public String chat(String question) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", config.getModel());
            requestBody.put("messages", List.of(
                    Map.of("role", "system", "content", "你是一个资深的数据分析专家，尤其在机器人管理领域和政务服务领域有所研究，" +
                            "你会负责对机器人的某些数据或者政务服务用户的某些数据进行分析，产出为一份文本报告。"),
                    Map.of("role", "user", "content", question)
            ));
            requestBody.put("stream", false);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + config.getApiKey());

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            String url = config.getBaseUrl() + "/v1/chat/completions";

            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            Map<String, Object> body = response.getBody();
            List<Map<String, Object>> choices = (List<Map<String, Object>>) body.get("choices");
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            String reply = (String) message.get("content");
            log.info("硅基流动回复：{}", reply);
            return reply;
        } catch (Exception e) {
            log.error("AI 服务调用异常", e);
            throw new RuntimeException("AI服务暂时不可用，请稍后重试");
        }
    }

    public String generateReport(String reportType, String timeRange, String data) {
        String prompt = String.format(
                "请根据以下数据生成一份%s报告。\n时间范围：%s\n数据：%s\n请输出分析+建议。",
                reportType, timeRange, data
        );
        return chat(prompt);
    }
}
