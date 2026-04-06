package com.ruoyi.data.ai.service;

import com.ruoyi.data.dashboard.controller.vo.WordCloudItem;

import java.util.List;

public interface AiAnalysisService {

    /**
     * 生成词云数据
     */
    List<WordCloudItem> generateWordCloud(List<String> texts);

}