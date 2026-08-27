package com.ruoyi.data.ai.util;

import com.alibaba.fastjson2.JSON;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonParseUtil {

    private static final Pattern JSON_ARRAY_PATTERN = Pattern.compile("\\[[\\s\\S]*\\]");

    /**
     * 清洗AI返回内容（去掉 ```json ```）
     */
    public static String clean(String text) {
        if (text == null) return null;

        return text
                .replaceAll("(?i)```\\s*json", "")
                .replaceAll("```", "")
                .trim();
    }

    public static String extractJsonArray(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        Matcher matcher = JSON_ARRAY_PATTERN.matcher(text);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }

    public static String repairCommonJsonIssues(String text) {
        if (text == null) {
            return null;
        }

        String s = text.trim();
        s = s.replaceAll("(?<=\\{|,)\\s*([a-zA-Z_][a-zA-Z0-9_]*)\"\\s*:", "\"$1\":");
        s = s.replaceAll(",\\s*([}\\]])", "$1");
        return s;
    }

    /**
     * 解析数组
     */
    public static <T> List<T> parseList(String text, Class<T> clazz) {
        String cleaned = clean(text);
        String extracted = extractJsonArray(cleaned);
        String json = extracted != null ? extracted : cleaned;
        try {
            return JSON.parseArray(json, clazz);
        } catch (Exception e) {
            String repaired = repairCommonJsonIssues(json);
            return JSON.parseArray(repaired, clazz);
        }
    }

    /**
     * 解析对象
     */
    public static <T> T parseObject(String text, Class<T> clazz) {
        String clean = clean(text);
        return JSON.parseObject(clean, clazz);
    }
}
