package com.ruoyi.data.clean.cleanExecuteStrategy;

import com.ruoyi.data.clean.domain.context.DataContext;
import com.ruoyi.data.clean.domain.enums.StatusMappingType;

public class StatusMappingStrategy implements CleanStrategy {

    private final StatusMappingType type;

    public StatusMappingStrategy(StatusMappingType type) {
        this.type = type;
    }

    @Override
    public String process(Object input, DataContext context) {

        Integer status = (Integer) input;

        if (type == StatusMappingType.KEEP_ORIGINAL) {
            return "原始状态";
        }

        if (status == null) return "未知";

        switch (status) {
            case 0: return "成功";
            case 1: return "失败";
            case 2: return "超时";
            default: return "未知";
        }
    }
}