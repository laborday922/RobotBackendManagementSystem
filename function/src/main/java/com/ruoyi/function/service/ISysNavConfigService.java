package com.ruoyi.function.service;

import com.ruoyi.function.domain.SysNavConfig;

public interface ISysNavConfigService {

    SysNavConfig getCurrentConfig();

    int saveConfig(SysNavConfig config);

    int updateMap(Long mapId);

    int startNavigation(String pointName);

    int emergencyStop();
}