package com.ruoyi.function.service.impl;

import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.function.domain.SysNavConfig;
import com.ruoyi.function.mapper.SysNavConfigMapper;
import com.ruoyi.function.service.ISysNavConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SysNavConfigServiceImpl implements ISysNavConfigService {

    @Autowired
    private SysNavConfigMapper navConfigMapper;

    @Override
    public SysNavConfig getCurrentConfig() {
        SysNavConfig config = navConfigMapper.selectCurrent();
        if (config == null) {
            config = new SysNavConfig();
            config.setVoiceType("default");
            config.setBeforeMsg("现在带您去社保窗口");
            config.setDuringMsg("请跟随我");
            config.setAfterMsg("已到达");
            navConfigMapper.insert(config);
        }
        return config;
    }

    @Override
    public int saveConfig(SysNavConfig config) {
        SysNavConfig existing = navConfigMapper.selectCurrent();
        if (existing != null) {
            config.setNavId(existing.getNavId());
            config.setUpdateTime(DateUtils.getNowDate());
            return navConfigMapper.update(config);
        } else {
            config.setCreateTime(DateUtils.getNowDate());
            return navConfigMapper.insert(config);
        }
    }

    @Override
    public int updateMap(Long mapId) {
        SysNavConfig config = getCurrentConfig();
        config.setMapId(mapId);
        config.setUpdateTime(DateUtils.getNowDate());
        return navConfigMapper.update(config);
    }

    @Override
    public int startNavigation(String pointName) {
        // 实际导航逻辑
        return 1;
    }

    @Override
    public int emergencyStop() {
        // 急停逻辑
        return 1;
    }
}