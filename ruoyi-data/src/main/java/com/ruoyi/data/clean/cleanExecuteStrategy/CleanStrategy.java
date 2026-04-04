package com.ruoyi.data.clean.cleanExecuteStrategy;

import com.ruoyi.data.clean.domain.context.DataContext;

public interface CleanStrategy {

    // 输入一行数据 → 返回处理结果
    Object process(Object input, DataContext context);
}