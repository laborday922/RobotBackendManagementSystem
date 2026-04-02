package com.ruoyi.function.service.impl;

import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.function.domain.SysReceptionConfig;
import com.ruoyi.function.mapper.SysReceptionConfigMapper;
import com.ruoyi.function.service.ISysReceptionConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SysReceptionConfigServiceImpl implements ISysReceptionConfigService {

    @Autowired
    private SysReceptionConfigMapper receptionConfigMapper;

    @Override
    public SysReceptionConfig getConfigByRobotId(Long robotId) {
        SysReceptionConfig config = receptionConfigMapper.selectByRobotId(robotId);
        if (config == null) {
            config = new SysReceptionConfig();
            config.setRobotId(robotId);
            config.setWelcome("请问关于公司：有哪些需要了解的？");
            config.setRepeat("您好");
            config.setIdle("真正的智能服务机器人 #机器人名称# 即将为您服务");
            config.setVipEnabled("1");
            config.setVipGreeting("尊敬的 #全名# #职称# 您好");
            config.setVipMulti("尊敬的贵宾们您们好");
            config.setStrangerEnabled("1");
            config.setStrangerGreeting("#性别# 您好");
            config.setStrangerMulti("先生女士们好");
            receptionConfigMapper.insert(config);
        }
        return config;
    }

    @Override
    @Transactional
    public int saveConfig(SysReceptionConfig config) {
        config.setUpdateTime(DateUtils.getNowDate());
        SysReceptionConfig existing = receptionConfigMapper.selectByRobotId(config.getRobotId());
        if (existing != null) {
            config.setConfigId(existing.getConfigId());
            return receptionConfigMapper.update(config);
        } else {
            config.setCreateTime(DateUtils.getNowDate());
            return receptionConfigMapper.insert(config);
        }
    }

    @Override
    @Transactional
    public int resetToDefault(Long robotId) {
        SysReceptionConfig config = new SysReceptionConfig();
        config.setRobotId(robotId);
        config.setWelcome("请问关于公司：有哪些需要了解的？");
        config.setRepeat("您好");
        config.setIdle("真正的智能服务机器人 #机器人名称# 即将为您服务");
        config.setVipEnabled("1");
        config.setVipGreeting("尊敬的 #全名# #职称# 您好");
        config.setVipMulti("尊敬的贵宾们您们好");
        config.setStrangerEnabled("1");
        config.setStrangerGreeting("#性别# 您好");
        config.setStrangerMulti("先生女士们好");
        config.setUpdateTime(DateUtils.getNowDate());

        SysReceptionConfig existing = receptionConfigMapper.selectByRobotId(robotId);
        if (existing != null) {
            config.setConfigId(existing.getConfigId());
            return receptionConfigMapper.update(config);
        } else {
            config.setCreateTime(DateUtils.getNowDate());
            return receptionConfigMapper.insert(config);
        }
    }
}
