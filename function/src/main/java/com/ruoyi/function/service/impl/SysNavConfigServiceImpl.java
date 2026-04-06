package com.ruoyi.function.service.impl;

import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.function.domain.SysNavConfig;
import com.ruoyi.function.enums.NavVoiceTypeEnum;
import com.ruoyi.function.mapper.SysNavConfigMapper;
import com.ruoyi.function.service.ISysNavConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SysNavConfigServiceImpl implements ISysNavConfigService {

    private static final Logger log = LoggerFactory.getLogger(SysNavConfigServiceImpl.class);

    @Autowired
    private SysNavConfigMapper navConfigMapper;

    @Override
    public SysNavConfig getCurrentConfig() {
        SysNavConfig config = navConfigMapper.selectCurrent();
        if (config == null) {
            config = createDefaultConfig();
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
        try {
            log.info("开始导航到：{}", pointName);
            // TODO: 实际导航逻辑调用
            // 获取当前配置，确定播报内容
            SysNavConfig config = getCurrentConfig();
            if (config != null && NavVoiceTypeEnum.DEFAULT.getCode().equals(config.getVoiceType())) {
                log.info("播报默认导航语音");
            }
            return 1;
        } catch (Exception e) {
            log.error("导航失败：{}", e.getMessage(), e);
            return 0;
        }
    }

    @Override
    public int emergencyStop() {
        try {
            log.info("紧急停止导航");
            // TODO: 急停逻辑调用
            return 1;
        } catch (Exception e) {
            log.error("急停失败：{}", e.getMessage(), e);
            return 0;
        }
    }

    /**
     * 创建默认配置
     */
    private SysNavConfig createDefaultConfig() {
        SysNavConfig config = new SysNavConfig();
        config.setVoiceType(NavVoiceTypeEnum.DEFAULT.getCode());
        config.setBeforeMsg("现在带您去社保窗口");
        config.setDuringMsg("请跟随我");
        config.setAfterMsg("已到达");
        config.setCreateTime(DateUtils.getNowDate());
        navConfigMapper.insert(config);
        return config;
    }
}