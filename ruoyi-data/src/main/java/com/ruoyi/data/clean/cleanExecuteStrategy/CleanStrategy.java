package com.ruoyi.data.clean.cleanExecuteStrategy;

import com.ruoyi.data.clean.domain.context.DataContext;

public interface CleanStrategy {

    void execute(DataContext context);

}