package com.ruoyi.data.clean.cleanExecuteStrategy;

import com.ruoyi.data.clean.domain.context.DataContext;

/**
 * 清洗策略接口（策略模式）。
 * 定义了统一的清洗契约，具体实现如：文本清洗、状态映射、去重等。
 */
public interface CleanStrategy {

    /**
     * 执行具体清洗逻辑。
     *
     * @param input   原始数据（类型由实现决定）
     * @param context 上下文，包含任务配置及中间数据
     * @return 清洗后的结果
     */
    Object process(Object input, DataContext context);
}