package com.ruoyi.function.service.impl;

import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.function.domain.SysNavConfig;
import com.ruoyi.function.mapper.SysNavConfigMapper;
import com.ruoyi.function.service.ISysNavConfigService;
import com.ruoyi.taskmgt.websocket.RobotWebSocketHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class SysNavConfigServiceImpl implements ISysNavConfigService {

    @Autowired
    private SysNavConfigMapper navConfigMapper;

    @Autowired(required = false)
    private RobotWebSocketHandler webSocketHandler;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public SysNavConfig getCurrentConfig() {
        return navConfigMapper.selectCurrent();
    }

    @Override
    public SysNavConfig getConfigByRobotId(String robotId) {
        if (StringUtils.isEmpty(robotId)) {
            return null;
        }
        return navConfigMapper.selectByRobotId(robotId);
    }

    @Override
    public int saveConfig(SysNavConfig config) {
        if (config == null || StringUtils.isEmpty(config.getRobotId())) {
            return 0;
        }

        SysNavConfig existing = navConfigMapper.selectByRobotId(config.getRobotId());
        int result;
        if (existing != null) {
            result = navConfigMapper.updateByRobotId(config);
        } else {
            result = navConfigMapper.insert(config);
        }

        // 保存成功后，通过 WebSocket 同步配置到机器人
        if (result > 0) {
            syncConfigToRobot(config);
        }

        return result;
    }

    /**
     * 通过 WebSocket 同步导航配置到机器人
     */
    private void syncConfigToRobot(SysNavConfig config) {
        if (webSocketHandler == null) {
            return;
        }

        try {
            Long robotId = Long.valueOf(config.getRobotId());
            if (!webSocketHandler.isOnline(robotId)) {
                return;
            }

            Map<String, Object> requestData = new HashMap<>();
            requestData.put("action", "sync_nav_config");
            requestData.put("configType", "navigation");
            requestData.put("voiceType", config.getVoiceType());
            requestData.put("beforeMsg", config.getBeforeMsg());
            requestData.put("duringMsg", config.getDuringMsg());
            requestData.put("afterMsg", config.getAfterMsg());

            String correlationId = UUID.randomUUID().toString();
            webSocketHandler.sendRequest(robotId, requestData, correlationId);

        } catch (NumberFormatException e) {
            // robotId 格式错误
        } catch (IOException e) {
            // 发送失败
        }
    }

    @Override
    public int updateMap(Long mapId) {
        return 0;
    }

    @Override
    public int startNavigation(String pointName) {
        return 1;
    }

    @Override
    public int emergencyStop() {
        return 1;
    }
}