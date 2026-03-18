package com.ruoyi.data.dashboard.service.impl;

import com.ruoyi.data.dashboard.controller.vo.RobotGeoInfo;
import com.ruoyi.data.dashboard.controller.vo.WordCloudItem;
import com.ruoyi.data.dashboard.mapper.DashboardMapper;
import com.ruoyi.data.dashboard.mapper.po.InteractionEvaluationPo;
import com.ruoyi.data.dashboard.mapper.po.RobotPositionLatestPo;
import com.ruoyi.data.dashboard.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private DashboardMapper dashboardMapper;

    @Override
    public List<WordCloudItem> getFeedbackWordCloud(Date startTime,
                                                    Date endTime,
                                                    String feedbackType) {

        Integer rating = convertRating(feedbackType);

        List<InteractionEvaluationPo> records =
                dashboardMapper.selectEvaluationTexts(startTime, endTime, rating);

        Map<String, Integer> wordCount = new HashMap<>();

        for (InteractionEvaluationPo record : records) {

            String text = record.getEvaluationText();

            if (text == null || text.isEmpty()) {
                continue;
            }

            String[] words = text.split(" ");

            for (String word : words) {

                if (word.length() < 2) {
                    continue;
                }

                wordCount.put(word,
                        wordCount.getOrDefault(word, 0) + 1);
            }
        }

        return wordCount.entrySet()
                .stream()
                .sorted((a, b) -> b.getValue() - a.getValue())
                .limit(50)
                .map(e -> {
                    WordCloudItem item = new WordCloudItem();
                    item.setName(e.getKey());
                    item.setValue(e.getValue());
                    return item;
                })
                .toList();
    }

    private Integer convertRating(String type) {

        if (type == null) {
            return null;
        }

        switch (type) {
            case "好评":
                return 2;
            case "差评":
                return 0;
            case "建议":
                return 1;
        }

        return null;
    }

    @Override
    public List<RobotGeoInfo> getRobotGeoDistribution() {

        List<RobotPositionLatestPo> pos =
                dashboardMapper.selectRobotLatestPositions();

        return pos.stream()
                .map(p -> {

                    RobotGeoInfo vo = new RobotGeoInfo();

                    vo.setRobotId(p.getRobotId());
                    vo.setLocationArea(p.getLocationArea());
                    vo.setSpecificLocation(p.getSpecificLocation());
                    vo.setCoordinateX(p.getCoordinateX());
                    vo.setCoordinateY(p.getCoordinateY());
                    vo.setMoveSpeed(p.getMoveSpeed());
                    vo.setStatusDesc(p.getStatusDesc());

                    return vo;

                })
                .toList();
    }
}