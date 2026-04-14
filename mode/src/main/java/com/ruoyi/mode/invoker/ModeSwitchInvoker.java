package com.ruoyi.mode.invoker;

import com.ruoyi.common.core.websocket.RobotWebSocketMessage;
import com.ruoyi.mode.invoker.dto.ModeSwitchRequest;
import com.ruoyi.mode.invoker.dto.ModeSwitchResponse;
import com.ruoyi.taskmgt.websocket.RobotWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 模式切换调用器 - 通过 WebSocket 向机器人下发模式切换命令
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ModeSwitchInvoker {

    private final RobotWebSocketHandler webSocketHandler;

    /**
     * 同步切换模式（等待响应）
     *
     * @param robotId 机器人ID
     * @param modeId 目标模式ID
     * @param modeName 目标模式名称
     * @param timeoutSeconds 超时时间（秒）
     * @return 切换结果
     */
    public ModeSwitchResponse switchModeSync(Long robotId, Long modeId, String modeName, int timeoutSeconds) {
        // 检查机器人是否在线
        if (!webSocketHandler.isOnline(robotId)) {
            log.warn("机器人 {} 不在线，无法切换模式", robotId);
            return ModeSwitchResponse.syncError("机器人不在线");
        }

        // 构建请求
        ModeSwitchRequest request = ModeSwitchRequest.of(modeId, modeName);
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("action", "mode_switch");
        requestData.put("modeId", modeId);
        requestData.put("modeName", modeName);
        requestData.put("immediate", true);

        String correlationId = UUID.randomUUID().toString();
        CompletableFuture<RobotWebSocketMessage> future = webSocketHandler.sendAndWaitRaw(robotId, requestData, correlationId, timeoutSeconds);

        try {
            RobotWebSocketMessage response = future.get(timeoutSeconds, TimeUnit.SECONDS);
            if (response.getData() != null) {
                // 解析响应
                Map<String, Object> respData = (Map<String, Object>) response.getData();
                Boolean success = (Boolean) respData.getOrDefault("success", false);
                if (success) {
                    return ModeSwitchResponse.syncSuccess(respData.get("data"));
                } else {
                    String errorMsg = (String) respData.getOrDefault("errorMsg", "切换失败");
                    return ModeSwitchResponse.syncError(errorMsg);
                }
            }
            return ModeSwitchResponse.syncError("响应数据为空");
        } catch (Exception e) {
            log.error("切换模式失败 robotId={}, modeId={}", robotId, modeId, e);
            return ModeSwitchResponse.syncError("调用失败: " + e.getMessage());
        }
    }

    /**
     * 异步切换模式（不等待响应，通过轮询或回调获取结果）
     *
     * @param robotId 机器人ID
     * @param modeId 目标模式ID
     * @param modeName 目标模式名称
     * @param callbackUrl 回调URL（可选）
     * @return 追踪ID
     */
    public String switchModeAsync(Long robotId, Long modeId, String modeName, String callbackUrl) {
        if (!webSocketHandler.isOnline(robotId)) {
            log.warn("机器人 {} 不在线，无法异步切换模式", robotId);
            return null;
        }

        String traceId = UUID.randomUUID().toString();
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("action", "mode_switch");
        requestData.put("modeId", modeId);
        requestData.put("modeName", modeName);
        requestData.put("mode", "ASYNC");
        requestData.put("traceId", traceId);
        if (callbackUrl != null) {
            requestData.put("callbackUrl", callbackUrl);
            requestData.put("mode", "CALLBACK");
        }

        String correlationId = UUID.randomUUID().toString();
        try {
            webSocketHandler.sendRequest(robotId, requestData, correlationId);
            log.info("已发送异步模式切换请求: robotId={}, modeId={}, traceId={}", robotId, modeId, traceId);
            return traceId;
        } catch (Exception e) {
            log.error("发送异步模式切换请求失败", e);
            return null;
        }
    }

    /**
     * 查询模式切换状态
     *
     * @param robotId 机器人ID
     * @param traceId 追踪ID
     * @return 状态信息
     */
    public ModeSwitchResponse querySwitchStatus(Long robotId, String traceId) {
        if (!webSocketHandler.isOnline(robotId)) {
            ModeSwitchResponse resp = new ModeSwitchResponse();
            resp.setSuccess(false);
            resp.setCompleted(false);
            resp.setStatus("ROBOT_OFFLINE");
            return resp;
        }

        Map<String, Object> queryReq = new HashMap<>();
        queryReq.put("action", "query");
        queryReq.put("traceId", traceId);

        String correlationId = UUID.randomUUID().toString();
        CompletableFuture<RobotWebSocketMessage> future = webSocketHandler.sendAndWaitRaw(robotId, queryReq, correlationId, 10);

        try {
            RobotWebSocketMessage response = future.get(10, TimeUnit.SECONDS);
            if (response.getData() != null) {
                Map<String, Object> respData = (Map<String, Object>) response.getData();
                ModeSwitchResponse resp = new ModeSwitchResponse();
                resp.setSuccess((Boolean) respData.getOrDefault("success", false));
                resp.setCompleted((Boolean) respData.getOrDefault("completed", false));
                resp.setProgress((Integer) respData.getOrDefault("progress", 0));
                resp.setStatus((String) respData.getOrDefault("status", "UNKNOWN"));
                resp.setData(respData.get("data"));
                resp.setErrorMsg((String) respData.get("errorMsg"));
                return resp;
            }
        } catch (Exception e) {
            log.error("查询模式切换状态失败", e);
        }

        ModeSwitchResponse resp = new ModeSwitchResponse();
        resp.setCompleted(false);
        resp.setStatus("QUERY_ERROR");
        return resp;
    }

    /**
     * 检查机器人是否在线
     */
    public boolean isRobotOnline(Long robotId) {
        return webSocketHandler.isOnline(robotId);
    }
}