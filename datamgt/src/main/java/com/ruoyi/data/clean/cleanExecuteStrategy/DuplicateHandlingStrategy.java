package com.ruoyi.data.clean.cleanExecuteStrategy;

import com.ruoyi.data.clean.domain.enums.DuplicateHandlingType;

import java.util.HashSet;
import java.util.Set;

public class DuplicateHandlingStrategy {

    private final DuplicateHandlingType type;
    private static final String PRIMARY_KEY = "id"; // 假设主键名为 id，如果可能变化可改为动态获取
    private final Set<String> seen = new HashSet<>();

    public DuplicateHandlingStrategy(DuplicateHandlingType type) {
        this.type = type;
        //测试
        System.out.println("DuplicateHandlingStrategy created with type: " + type);
    }

    /**
     * 是否保留该条数据
     */
    public boolean shouldKeep(String text) {

        if (text == null) return false;

        switch (type) {

            case KEEP_ORIGINAL:
                return true;

            case KEEP_FIRST:
                if (seen.contains(text)) {
                    return false;
                }
                seen.add(text);
                return true;

            case DELETE_ALL:
                // 简化版本（当前阶段先不实现复杂逻辑）
                return true;

            default:
                return true;
        }
    }
}