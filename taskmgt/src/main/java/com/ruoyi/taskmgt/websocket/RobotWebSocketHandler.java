package com.ruoyi.taskmgt.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.common.core.websocket.RobotWebSocketMessage;
import com.ruoyi.robots.controller.dto.RobotStatusDto;
import com.ruoyi.robots.service.IRobotsService;
import com.ruoyi.taskmgt.event.WebSocketAsyncResultEvent;
import com.ruoyi.taskmgt.invoker.dto.RobotTaskResponse;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class RobotWebSocketHandler extends TextWebSocketHandler {

    @Autowired
    private IRobotsService robotService;
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    private final ObjectMapper objectMapper = new ObjectMapper();
    @Getter
    private final Map<Long, WebSocketSession> robotSessions = new ConcurrentHashMap<>();
    private final Map<String, CompletableFuture<RobotTaskResponse>> pendingResponses = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("新WebSocket连接: {}", session.getId());
        session.getAttributes().put("authenticated", false);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        RobotWebSocketMessage wsMsg = objectMapper.readValue(payload, RobotWebSocketMessage.class);
        String type = wsMsg.getType();

        if ("AUTH".equals(type)) {
            Long robotId = wsMsg.getRobotId();
            if (robotId != null && robotService.selectRobotsById(robotId) != null) {
                robotSessions.put(robotId, session);
                session.getAttributes().put("robotId", robotId);
                session.getAttributes().put("authenticated", true);
                // 更新在线状态和心跳时间
                RobotStatusDto robot = new RobotStatusDto();
                robot.setId(robotId);
                robot.setStatus(1);
                robot.setLastHeartbeatTime(new Date());
                robotService.updateRobotStatus(robot);
                log.info("机器人 {} 认证成功", robotId);
                sendMessage(session, RobotWebSocketMessage.authSuccess());
            } else {
                log.warn("认证失败，无效robotId: {}", robotId);
                session.close(CloseStatus.BAD_DATA);
            }
        } else if ("RESPONSE".equals(type)) {
            String correlationId = wsMsg.getCorrelationId();
            CompletableFuture<RobotTaskResponse> future = pendingResponses.remove(correlationId);
            if (future != null) {
                RobotTaskResponse response = objectMapper.convertValue(wsMsg.getData(), RobotTaskResponse.class);
                future.complete(response);
            } else {
                log.warn("未找到等待中的请求: {}", correlationId);
            }
        } else if ("ASYNC_RESULT".equals(type)) {
            String traceId = wsMsg.getTraceId();
            boolean success = wsMsg.getSuccess() != null && wsMsg.getSuccess();
            eventPublisher.publishEvent(new WebSocketAsyncResultEvent(this,traceId, success, wsMsg.getData(), wsMsg.getErrorMsg()));
        } else if ("HEARTBEAT".equals(type)) {
            Long robotId = (Long) session.getAttributes().get("robotId");
            if (robotId != null) {
                RobotStatusDto robot = new RobotStatusDto();
                robot.setId(robotId);
                robot.setLastHeartbeatTime(new Date());
                robotService.updateRobotStatus(robot);
                sendMessage(session, RobotWebSocketMessage.heartbeatAck());
            }
        } else {
            log.warn("未知消息类型: {}", type);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long robotId = (Long) session.getAttributes().get("robotId");
        if (robotId != null) {
            robotSessions.remove(robotId);
            RobotStatusDto robot = new RobotStatusDto();
            robot.setId(robotId);
            robot.setStatus(0);
            robot.setLastHeartbeatTime(new Date(0));
            robotService.updateRobotStatus(robot);
            log.info("机器人 {} 断开连接", robotId);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        Long robotId = (Long) session.getAttributes().get("robotId");
        if (robotId != null) {
            robotSessions.remove(robotId);
            RobotStatusDto robot = new RobotStatusDto();
            robot.setId(robotId);
            robot.setStatus(0);
            robot.setLastHeartbeatTime(new Date(0));
            robotService.updateRobotStatus(robot);
        }
        log.error("WebSocket传输错误", exception);
    }

    public void sendMessage(WebSocketSession session, RobotWebSocketMessage message) throws IOException {
        String json = objectMapper.writeValueAsString(message);
        session.sendMessage(new TextMessage(json));
    }

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

    public boolean isOnline(Long robotId) {
        WebSocketSession session = robotSessions.get(robotId);
        return session != null && session.isOpen();
    }
}