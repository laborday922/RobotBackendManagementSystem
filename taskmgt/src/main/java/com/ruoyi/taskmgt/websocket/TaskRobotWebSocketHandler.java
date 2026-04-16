package com.ruoyi.taskmgt.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.common.core.websocket.RobotWebSocketMessage;
import com.ruoyi.robots.service.IRobotsService;
import com.ruoyi.robots.websocket.RobotWebSocketHandler;
import com.ruoyi.taskmgt.invoker.dto.RobotTaskResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 任务模块的 WebSocket 处理器
 */
@Slf4j
@Component
public class TaskRobotWebSocketHandler extends RobotWebSocketHandler {

    /** correlationId -> CompletableFuture<RobotTaskResponse> 映射 */
    private final Map<String, CompletableFuture<RobotTaskResponse>> pendingResponses = new ConcurrentHashMap<>();

    public TaskRobotWebSocketHandler(IRobotsService robotService, ApplicationEventPublisher eventPublisher) {
        super(robotService, eventPublisher);
    }

    @Override
    protected void handleResponse(RobotWebSocketMessage wsMsg) {
        String correlationId = wsMsg.getCorrelationId();
        CompletableFuture<RobotTaskResponse> taskFuture = pendingResponses.remove(correlationId);
        if (taskFuture != null) {
            RobotTaskResponse response = objectMapper.convertValue(wsMsg.getData(), RobotTaskResponse.class);
            taskFuture.complete(response);
            return;
        }
        super.handleResponse(wsMsg);
    }

    /**
     * 发送请求并等待 RobotTaskResponse 响应
     *
     * @param robotId 机器人ID
     * @param request 请求数据
     * @param correlationId 关联ID
     * @param timeoutSeconds 超时时间（秒）
     * @return RobotTaskResponse 的 Future
     */
    public CompletableFuture<RobotTaskResponse> sendAndWait(Long robotId, Object request, String correlationId, long timeoutSeconds) {
        WebSocketSession session = robotSessions.get(robotId);
        if (session == null || !session.isOpen()) {
            CompletableFuture<RobotTaskResponse> future = new CompletableFuture<>();
            future.completeExceptionally(new RuntimeException("机器人未连接"));
            return future;
        }

        CompletableFuture<RobotTaskResponse> future = new CompletableFuture<>();
        pendingResponses.put(correlationId, future);

        try {
            RobotWebSocketMessage msg = RobotWebSocketMessage.request(correlationId, request);
            sendMessage(session, msg);
            future.orTimeout(timeoutSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            pendingResponses.remove(correlationId);
            future.completeExceptionally(e);
        }
        return future;
    }
}