package com.ruoyi.data.ai.service.impl;

import com.alibaba.fastjson2.JSON;
import com.ruoyi.data.ai.domain.bo.CategoryCount;
import com.ruoyi.data.ai.domain.bo.TimeSeriesData;
import com.ruoyi.data.ai.mapper.IssueClassificationMapper;
import com.ruoyi.data.ai.service.IssueClassificationService;
import com.ruoyi.data.ai.service.TongYiService;
import com.ruoyi.data.ai.util.JsonParseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class IssueClassificationServiceImpl implements IssueClassificationService {

    @Autowired
    private IssueClassificationMapper mapper;

    @Autowired
    private TongYiService tongYiService;

    @Override
    public List<CategoryCount> getIssueDistribution(String timeRange) {
        Date[] range = parseTimeRange(timeRange);

        List<Map<String, Object>> raw = mapper.selectRawInteractions(range[0], range[1]);
        String data = JSON.toJSONString(raw);
        System.out.print("问题分析数据："+data);

        String prompt = "你是数据分析专家，请对以下机器人交互数据进行问题分类统计。\n" +
                "要求：\n" +
                "1. 自动归类问题类型（技术问题/操作问题/设备问题/其他）\n" +
                "2. 统计每类数量\n" +
                "3. 为每个类别提供一条简短建议（15字以内）\n" +
                "4. 计算每类数量占总数的百分比（如 25.5 表示 25.5%）\n" +
                "5. 只返回 JSON 数组，不要任何解释\n" +
                "格式如下：\n" +
                "[{\"category\":\"技术问题\",\"count\":10,\"suggestion\":\"优化系统稳定性\",\"percentage\":30}]\n\n" +
                "数据如下：\n" + data;

        String aiResult = tongYiService.chat(prompt);

        try {
            return JsonParseUtil.parseList(aiResult, CategoryCount.class);
        } catch (Exception e) {
            throw new RuntimeException("AI返回结果解析失败：" + aiResult);
        }
    }

    @Override
    public TimeSeriesData getIssueTrend(String category, String granularity, String timeRange) {
        Date[] range = parseTimeRange(timeRange);

        Integer type = mapCategory(category);
        List<Map<String, Object>> raw = mapper.selectRawByCategory(type, range[0], range[1]);

        String data = JSON.toJSONString(raw);

        String prompt = String.format(
                "你是数据分析专家，请分析以下数据的时间趋势。\n" +
                        "分类：%s\n" +
                        "时间粒度：%s（hour/day/week）\n" +
                        "要求：\n" +
                        "1. 按时间统计数量\n" +
                        "2. 只返回JSON对象，不要解释\n" +
                        "格式：{\"timeList\":[],\"valueList\":[]}\n\n" +
                        "数据：%s",
                category, granularity, data
        );

        String aiResult = tongYiService.chat(prompt);

        try {
            return JsonParseUtil.parseObject(aiResult, TimeSeriesData.class);
        } catch (Exception e) {
            throw new RuntimeException("AI返回结果解析失败：" + aiResult);
        }
    }

    // ================= 工具方法 =================

    private Date[] parseTimeRange(String timeRange) {
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start;

        switch (timeRange) {
            case "last_7_days":
                start = end.minusDays(7);
                break;
            case "last_30_days":
                start = end.minusDays(30);
                break;
            default:
                start = end.minusDays(7);
        }

        return new Date[]{
                Date.from(start.atZone(ZoneId.systemDefault()).toInstant()),
                Date.from(end.atZone(ZoneId.systemDefault()).toInstant())
        };
    }

    private Integer mapCategory(String category) {
        switch (category) {
            case "配送": return 0;
            case "清洁": return 1;
            case "巡检": return 2;
            case "维保": return 3;
            case "安防": return 4;
            default: return 0;
        }
    }

}