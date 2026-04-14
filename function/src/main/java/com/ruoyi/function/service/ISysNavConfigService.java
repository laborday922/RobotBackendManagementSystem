package com.ruoyi.function.service;

import com.ruoyi.function.domain.SysNavConfig;

public interface ISysNavConfigService {

    /**
     * 获取当前配置（旧方法）
     */
    SysNavConfig getCurrentConfig();

    /**
     * 根据机器人ID获取配置
     */
    SysNavConfig getConfigByRobotId(String robotId);

    /**
     * 保存配置
     */
    int saveConfig(SysNavConfig config);

    int updateMap(Long mapId);

    int startNavigation(String pointName);

    int emergencyStop();
}