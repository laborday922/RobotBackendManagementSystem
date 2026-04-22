package com.ruoyi.taskmgt.invoker;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.common.core.websocket.RobotWebSocketMessage;
import com.ruoyi.taskmgt.invoker.dto.RobotTaskRequest;
import com.ruoyi.taskmgt.invoker.dto.RobotTaskResponse;
import com.ruoyi.taskmgt.invoker.dto.TaskStatusResponse;
import com.ruoyi.taskmgt.websocket.TaskRobotWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@RequiredArgsConstructor
public class RobotInvoker {

    private final TaskRobotWebSocketHandler webSocketHandler;
    private final ObjectMapper objectMapper;

    /**
     * 执行任务（同步等待响应）
     */
    public RobotTaskResponse execute(Long robotId, RobotTaskRequest request) {
        if (!webSocketHandler.isOnline(robotId)) {
            log.warn("机器人 {} 不在线", robotId);
            RobotTaskResponse errorResp = new RobotTaskResponse();
            errorResp.setSuccess(false);
            errorResp.setErrorMsg("机器人不在线");
            errorResp.setMode("SYNC");
            return errorResp;
        }
        String correlationId = UUID.randomUUID().toString();
        CompletableFuture<RobotTaskResponse> future = webSocketHandler.sendAndWait(robotId, request, correlationId, 30);
        try {
            return future.get(30, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("调用机器人失败 robotId={}", robotId, e);
            RobotTaskResponse errorResp = new RobotTaskResponse();
            errorResp.setSuccess(false);
            errorResp.setErrorMsg("调用失败: " + e.getMessage());
            errorResp.setMode("SYNC");
            return errorResp;
        }
    }

    /**
     * 查询异步任务状态
     */
    public TaskStatusResponse queryStatus(Long robotId, String traceId) {
        if (!webSocketHandler.isOnline(robotId)) {
            TaskStatusResponse resp = new TaskStatusResponse();
            resp.setCompleted(false);
            resp.setStatus("ROBOT_OFFLINE");
            return resp;
        }
        Map<String, String> queryReq = Map.of("traceId", traceId, "action", "query");
        String correlationId = UUID.randomUUID().toString();
        CompletableFuture<RobotWebSocketMessage> rawFuture = webSocketHandler.sendAndWaitRaw(robotId, queryReq, correlationId, 10);
        try {
            RobotWebSocketMessage rawMsg = rawFuture.get(10, TimeUnit.SECONDS);
            // 从 rawMsg.getData() 转换为 TaskStatusResponse
            return objectMapper.convertValue(rawMsg.getData(), TaskStatusResponse.class);
        } catch (Exception e) {
            log.error("查询状态失败 robotId={}, traceId={}", robotId, traceId, e);
            TaskStatusResponse resp = new TaskStatusResponse();
            resp.setCompleted(false);
            resp.setStatus("QUERY_ERROR");
            return resp;
        }
    }

    /**
     * 获取机器人是否在线
     */
    public boolean isRobotOnline(Long robotId) {
        return webSocketHandler.isOnline(robotId);
    }

    /**
     * 发送控制指令（同步等待确认）
     */
    public boolean sendCommand(Long robotId, Map<String, Object> command, long timeoutSeconds) {
        if (!webSocketHandler.isOnline(robotId)) {
            log.warn("机器人 {} 不在线，无法发送控制指令", robotId);
            return false;
        }
        String correlationId = UUID.randomUUID().toString();
        CompletableFuture<RobotWebSocketMessage> future = webSocketHandler.sendAndWaitRaw(robotId, command, correlationId, timeoutSeconds);
        try {
            RobotWebSocketMessage response = future.get(timeoutSeconds, TimeUnit.SECONDS);
            Object data = response.getData();
            boolean success = false;

            if (data instanceof Map) {
                // 直接是 Map 类型
                Map<?, ?> dataMap = (Map<?, ?>) data;
                Object successObj = dataMap.get("success");
                success = Boolean.TRUE.equals(successObj) || "true".equalsIgnoreCase(String.valueOf(successObj));
            } else if (data != null) {
                // 非 Map 类型，尝试用 ObjectMapper 转换
                try {
                    Map<String, Object> dataMap = objectMapper.readValue(
                            objectMapper.writeValueAsString(data),
                            new TypeReference<Map<String, Object>>() {}
                    );
                    Object successObj = dataMap.get("success");
                    success = Boolean.TRUE.equals(successObj) || "true".equalsIgnoreCase(String.valueOf(successObj));
                } catch (Exception e) {
                    log.warn("无法解析响应数据中的 success 字段: {}", data, e);
                }
            }

            if (!success) {
                log.warn("机器人 {} 控制指令执行失败: {}", robotId, response.getErrorMsg());
            }
            return success;
        } catch (Exception e) {
            log.error("发送控制指令失败 robotId={}, command={}", robotId, command, e);
            return false;
        }
    }
}