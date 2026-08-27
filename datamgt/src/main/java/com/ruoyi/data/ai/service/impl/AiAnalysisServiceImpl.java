package com.ruoyi.data.ai.service.impl;

import com.alibaba.fastjson2.JSON;
import com.ruoyi.data.ai.service.AiAnalysisService;
import com.ruoyi.data.ai.service.SiliconFlowService;
import com.ruoyi.data.ai.util.JsonParseUtil;
import com.ruoyi.data.dashboard.controller.vo.WordCloudItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class AiAnalysisServiceImpl implements AiAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(AiAnalysisServiceImpl.class);

    @Autowired
    private SiliconFlowService siliconFlowService;

    @Override
    public List<WordCloudItem> generateWordCloud(List<String> texts) {

        if (texts == null || texts.isEmpty()) {
            return Collections.emptyList();
        }

        String data = JSON.toJSONString(texts);

        // 🔥 Prompt统一收口在这里
        String prompt = "你是数据分析专家，请对以下用户评价生成词云数据。\n" +
                "要求：\n" +
                "1. 中文分词\n" +
                "2. 过滤停用词（的、了、是等）\n" +
                "3. 过滤敏感词\n" +
                "4. 统计词频\n" +
                "5. 返回前50个高频词\n" +
                "6. 只返回JSON数组，不要解释\n" +
                "格式：[{\"name\":\"卡顿\",\"value\":10}]\n\n" +
                "数据如下：\n" + data;

        String aiResult = siliconFlowService.chat(prompt);

        try {
            return JsonParseUtil.parseList(aiResult, WordCloudItem.class);
        } catch (Exception e) {
            String safe = aiResult == null ? "" : aiResult;
            if (safe.length() > 2000) {
                safe = safe.substring(0, 2000);
            }
            log.warn("AI词云解析失败，已降级为空列表。aiResult={}", safe, e);
            return Collections.emptyList();
        }
    }
}
