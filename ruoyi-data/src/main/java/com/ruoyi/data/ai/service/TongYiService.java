package com.ruoyi.data.ai.service;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.ruoyi.data.ai.config.TongYiConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class TongYiService {
    private static final Logger log = LoggerFactory.getLogger(TongYiService.class);

    @Autowired
    private TongYiConfig tongYiConfig;

    public String chat(String question) {
        try {
            Generation gen = new Generation();

            Message systemMsg = Message.builder()
                    .role(Role.SYSTEM.getValue())
                    .content("你是一个资深的数据分析专家，尤其在机器人管理领域和政务服务领域有所研究，" +
                            "你会负责对机器人的某些数据或者政务服务用户的某些数据进行分析，产出为一份文本报告。")
                    .build();
            Message userMsg = Message.builder()
                    .role(Role.USER.getValue())
                    .content(question)
                    .build();
            List<Message> messages = Arrays.asList(systemMsg, userMsg);

            GenerationParam param = GenerationParam.builder()
                    .apiKey(tongYiConfig.getApiKey())
                    .model(tongYiConfig.getModel())
                    .messages(messages)
                    .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                    .enableSearch(tongYiConfig.isEnableSearch())
                    .build();

            GenerationResult result = gen.call(param);
            String reply = result.getOutput().getChoices().get(0).getMessage().getContent();
            log.info("通义千问回复：{}", reply);
            return reply;
        } catch (ApiException | NoApiKeyException | InputRequiredException e) {
            log.error("AI 服务调用异常", e);
            throw new RuntimeException("AI服务暂时不可用，请稍后重试");
        }
    }
}