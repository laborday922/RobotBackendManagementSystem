package com.ruoyi.function.service.impl;

import com.ruoyi.common.threadlocal.TenantContext;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.function.domain.SysReceptionConfig;
import com.ruoyi.function.mapper.SysReceptionConfigMapper;
import com.ruoyi.function.service.ISysReceptionConfigService;
import com.ruoyi.taskmgt.websocket.RobotWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.ruoyi.common.utils.SecurityUtils.isAdmin;

@Service
public class SysReceptionConfigServiceImpl implements ISysReceptionConfigService {

    @Autowired
    private SysReceptionConfigMapper receptionConfigMapper;

    @Autowired(required = false)
    private RobotWebSocketHandler webSocketHandler;

    @Override
    public SysReceptionConfig getConfigByRobotId(Long robotId) {
        SysReceptionConfig config = receptionConfigMapper.selectByRobotId(robotId);
        Long tenantId = TenantContext.get();
        if (config == null) {
            config = createDefaultConfig(robotId, tenantId);
            receptionConfigMapper.insert(config);
        } else if (!isAdmin(tenantId) && config.getTenantId() != null && !config.getTenantId().equals(tenantId)) {
            config = createDefaultConfig(robotId, tenantId);
        }
        return config;
    }

    private SysReceptionConfig createDefaultConfig(Long robotId, Long tenantId) {
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
        config.setTenantId(tenantId);
        return config;
    }

    @Override
    @Transactional
    public int saveConfig(SysReceptionConfig config) {
        config.setUpdateTime(DateUtils.getNowDate());
        config.setTenantId(TenantContext.get());

        SysReceptionConfig existing = receptionConfigMapper.selectByRobotId(config.getRobotId());
        int result;
        if (existing != null) {
            config.setConfigId(existing.getConfigId());
            result = receptionConfigMapper.update(config);
        } else {
            config.setCreateTime(DateUtils.getNowDate());
            result = receptionConfigMapper.insert(config);
        }

        // 保存成功后，通过 WebSocket 同步配置到机器人
        if (result > 0) {
            syncConfigToRobot(config);
        }

        return result;
    }

    /**
     * 通过 WebSocket 同步接待配置到机器人
     */
    private void syncConfigToRobot(SysReceptionConfig config) {
        if (webSocketHandler == null) {
            return;
        }

        try {
            Long robotId = config.getRobotId();
            if (!webSocketHandler.isOnline(robotId)) {
                return;
            }

            Map<String, Object> requestData = new HashMap<>();
            requestData.put("action", "sync_reception_config");
            requestData.put("configType", "reception");
            requestData.put("welcome", config.getWelcome());
            requestData.put("repeat", config.getRepeat());
            requestData.put("idle", config.getIdle());
            requestData.put("vipEnabled", config.getVipEnabled());
            requestData.put("vipGreeting", config.getVipGreeting());
            requestData.put("vipMulti", config.getVipMulti());
            requestData.put("strangerEnabled", config.getStrangerEnabled());
            requestData.put("strangerGreeting", config.getStrangerGreeting());
            requestData.put("strangerMulti", config.getStrangerMulti());

            String correlationId = UUID.randomUUID().toString();
            webSocketHandler.sendRequest(robotId, requestData, correlationId);

        } catch (IOException e) {
            // 发送失败
        }
    }

    @Override
    @Transactional
    public int resetToDefault(Long robotId) {
        SysReceptionConfig config = createDefaultConfig(robotId, TenantContext.get());
        config.setUpdateTime(DateUtils.getNowDate());

        SysReceptionConfig existing = receptionConfigMapper.selectByRobotId(robotId);
        int result;
        if (existing != null) {
            config.setConfigId(existing.getConfigId());
            result = receptionConfigMapper.update(config);
        } else {
            config.setCreateTime(DateUtils.getNowDate());
            result = receptionConfigMapper.insert(config);
        }

        if (result > 0) {
            syncConfigToRobot(config);
        }

        return result;
    }
}