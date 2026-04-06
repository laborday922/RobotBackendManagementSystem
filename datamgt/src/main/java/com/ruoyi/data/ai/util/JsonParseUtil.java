package com.ruoyi.data.ai.util;

import com.alibaba.fastjson2.JSON;

import java.util.List;

public class JsonParseUtil {

    /**
     * 清洗AI返回内容（去掉 ```json ```）
     */
    public static String clean(String text) {
        if (text == null) return null;

        return text
                .replaceAll("```json", "")
                .replaceAll("```", "")
                .trim();
    }

    /**
     * 解析数组
     */
    public static <T> List<T> parseList(String text, Class<T> clazz) {
        String clean = clean(text);
        return JSON.parseArray(clean, clazz);
    }

    /**
     * 解析对象
     */
    public static <T> T parseObject(String text, Class<T> clazz) {
        String clean = clean(text);
        return JSON.parseObject(clean, clazz);
    }
}