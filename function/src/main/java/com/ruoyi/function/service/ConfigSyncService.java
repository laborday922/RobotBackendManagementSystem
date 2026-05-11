package com.ruoyi.function.service;

import com.ruoyi.robots.websocket.RobotWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 配置同步服务 - 通过 WebSocket 将配置同步到机器人
 */
@Service
public class ConfigSyncService {

    @Autowired(required = false)
    private RobotWebSocketHandler webSocketHandler;

    /**
     * 同步配置到机器人
     *
     * @param robotId 机器人ID
     * @param configType 配置类型（navigation/reception/tour）
     * @param configData 配置数据
     * @return 是否发送成功
     */
    public boolean syncConfig(Long robotId, String configType, Map<String, Object> configData) {
        if (webSocketHandler == null) {
            return false;
        }

        try {
            if (!webSocketHandler.isOnline(robotId)) {
                return false;
            }

            Map<String, Object> requestData = new HashMap<>();
            requestData.put("action", "sync_config");
            requestData.put("configType", configType);
            requestData.putAll(configData);

            String correlationId = UUID.randomUUID().toString();
            webSocketHandler.sendRequest(robotId, requestData, correlationId);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * 通知机器人删除配置
     */
    public boolean notifyDelete(Long robotId, String configType, Long configId) {
        if (webSocketHandler == null) {
            return false;
        }

        try {
            if (!webSocketHandler.isOnline(robotId)) {
                return false;
            }

            Map<String, Object> requestData = new HashMap<>();
            requestData.put("action", "delete_config");
            requestData.put("configType", configType);
            requestData.put("configId", configId);

            String correlationId = UUID.randomUUID().toString();
            webSocketHandler.sendRequest(robotId, requestData, correlationId);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}