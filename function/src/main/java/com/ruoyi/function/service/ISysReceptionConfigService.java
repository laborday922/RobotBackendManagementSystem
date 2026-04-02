package com.ruoyi.function.service;

import com.ruoyi.function.domain.SysReceptionConfig;

public interface ISysReceptionConfigService {

    SysReceptionConfig getConfigByRobotId(Long robotId);

    int saveConfig(SysReceptionConfig config);

    int resetToDefault(Long robotId);
}