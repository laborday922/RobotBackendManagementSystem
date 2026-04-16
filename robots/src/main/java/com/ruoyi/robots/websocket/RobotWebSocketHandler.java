package com.ruoyi.robots.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.common.core.websocket.RobotWebSocketMessage;
import com.ruoyi.robots.controller.dto.RobotStatusDto;
import com.ruoyi.robots.event.WebSocketAsyncResultEvent;
import com.ruoyi.robots.service.IRobotsService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

/**
 * 通用 WebSocket 处理器（机器人管理模块）
 * 负责机器人连接认证、心跳、会话管理、原始消息收发。
 * 子类可继承并覆盖特定消息的处理逻辑。
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class RobotWebSocketHandler extends TextWebSocketHandler {

    protected final IRobotsService robotService;
    protected final ApplicationEventPublisher eventPublisher;
    protected final ObjectMapper objectMapper = new ObjectMapper();

    /** 机器人ID -> WebSocketSession 映射 */
    @Getter
    protected final Map<Long, WebSocketSession> robotSessions = new ConcurrentHashMap<>();

    /** correlationId -> CompletableFuture<RobotWebSocketMessage> 映射（通用原始响应） */
    protected final Map<String, CompletableFuture<RobotWebSocketMessage>> pendingRawResponses = new ConcurrentHashMap<>();

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
            handleAuth(session, wsMsg);
        } else if ("RESPONSE".equals(type)) {
            handleResponse(wsMsg);
        } else if ("ASYNC_RESULT".equals(type)) {
            handleAsyncResult(wsMsg);
        } else if ("HEARTBEAT".equals(type)) {
            handleHeartbeat(session, wsMsg);
        } else if("CLOSE".equals(type)){
            closeSession(session, CloseStatus.NORMAL);
        }
        else {
            log.warn("未知消息类型: {}", type);
        }
    }

    /**
     * 处理认证消息
     */
    protected void handleAuth(WebSocketSession session, RobotWebSocketMessage wsMsg) throws IOException {
        Long robotId = wsMsg.getRobotId();
        if (robotId != null && robotService.selectRobotsById(robotId) != null) {
            robotSessions.put(robotId, session);
            session.getAttributes().put("robotId", robotId);
            session.getAttributes().put("authenticated", true);

            // 更新在线状态和心跳时间
            RobotStatusDto robot = new RobotStatusDto();
            robot.setId(robotId);
            robot.setStatus(1);
            robot.setHardwareStatus(0);
            robot.setLastHeartbeatTime(new Date());
            robotService.updateRobotStatus(robot);
            sendMessage(session, RobotWebSocketMessage.authSuccess());
            log.info("机器人 {} 认证成功", robotId);
        } else {
            log.warn("认证失败，无效robotId: {}", robotId);
            session.close(CloseStatus.BAD_DATA);
        }
    }

    /**
     * 直接关闭指定的 WebSocket 会话
     * @param session 要关闭的会话
     * @param status 关闭状态（如 CloseStatus.NORMAL）
     */
    public void closeSession(WebSocketSession session, CloseStatus status) {
        if (session != null && session.isOpen()) {
            try {
                session.close(status);
                log.info("主动关闭会话: {}, 状态: {}", session.getId(), status);
            } catch (IOException e) {
                log.error("关闭会话失败: {}", session.getId(), e);
            }
        }
    }

    /**
     * 管理端根据机器人ID主动关闭连接
     * @param robotId 机器人ID
     * @param status 关闭状态
     * @return true 如果成功关闭，false 如果机器人不在线
     */
    public boolean closeRobotConnection(Long robotId, CloseStatus status) {
        WebSocketSession session = robotSessions.get(robotId);
        if (session == null || !session.isOpen()) {
            log.warn("机器人 {} 不在线，无法关闭连接", robotId);
            return false;
        }
        closeSession(session, status);
        return true;
    }


    /**
     * 处理响应消息（通用版本，完成原始响应Future）
     * 子类可重写以进行特定类型的转换
     */
    protected void handleResponse(RobotWebSocketMessage wsMsg) {
        String correlationId = wsMsg.getCorrelationId();
        CompletableFuture<RobotWebSocketMessage> rawFuture = pendingRawResponses.remove(correlationId);
        if (rawFuture != null) {
            rawFuture.complete(wsMsg);
        } else {
            log.warn("未找到等待中的请求: {}", correlationId);
        }
    }

    /**
     * 处理异步结果推送
     */
    protected void handleAsyncResult(RobotWebSocketMessage wsMsg) {
        String traceId = wsMsg.getTraceId();
        boolean success = wsMsg.getSuccess() != null && wsMsg.getSuccess();
        log.info("收到异步结果: traceId={}, success={}", traceId, success);
        eventPublisher.publishEvent(new WebSocketAsyncResultEvent(this, traceId, success, wsMsg.getData(), wsMsg.getErrorMsg()));
    }

    /**
     * 处理心跳消息
     */
    protected void handleHeartbeat(WebSocketSession session, RobotWebSocketMessage wsMsg) throws IOException {
        Long robotId = (Long) session.getAttributes().get("robotId");
        if (robotId != null) {
            RobotStatusDto robot = new RobotStatusDto();
            robot.setId(robotId);
            robot.setLastHeartbeatTime(new Date());
            robotService.updateRobotStatus(robot);
            sendMessage(session, RobotWebSocketMessage.heartbeatAck());
            log.debug("收到心跳 from robotId={}", robotId);
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

    /**
     * 发送消息给指定会话
     */
    public void sendMessage(WebSocketSession session, RobotWebSocketMessage message) throws IOException {
        String json = objectMapper.writeValueAsString(message);
        session.sendMessage(new TextMessage(json));
    }

    /**
     * 发送请求并等待原始响应（返回 RobotWebSocketMessage）
     *
     * @param robotId 机器人ID
     * @param request 请求数据
     * @param correlationId 关联ID
     * @param timeoutSeconds 超时时间（秒）
     * @return 原始响应Future
     */
    public CompletableFuture<RobotWebSocketMessage> sendAndWaitRaw(Long robotId, Object request, String correlationId, long timeoutSeconds) {
        WebSocketSession session = robotSessions.get(robotId);
        if (session == null || !session.isOpen()) {
            CompletableFuture<RobotWebSocketMessage> future = new CompletableFuture<>();
            future.completeExceptionally(new RuntimeException("机器人未连接"));
            return future;
        }

        CompletableFuture<RobotWebSocketMessage> future = new CompletableFuture<>();
        pendingRawResponses.put(correlationId, future);

        try {
            RobotWebSocketMessage msg = RobotWebSocketMessage.request(correlationId, request);
            sendMessage(session, msg);
            future.orTimeout(timeoutSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            pendingRawResponses.remove(correlationId);
            future.completeExceptionally(e);
        }
        return future;
    }

    /**
     * 发送请求（不等待响应）
     *
     * @param robotId 机器人ID
     * @param request 请求数据
     * @param correlationId 关联ID
     * @throws IOException 发送异常
     */
    public void sendRequest(Long robotId, Object request, String correlationId) throws IOException {
        WebSocketSession session = robotSessions.get(robotId);
        if (session == null || !session.isOpen()) {
            throw new IOException("机器人未连接");
        }
        RobotWebSocketMessage msg = RobotWebSocketMessage.request(correlationId, request);
        sendMessage(session, msg);
    }

    /**
     * 检查机器人是否在线
     */
    public boolean isOnline(Long robotId) {
        WebSocketSession session = robotSessions.get(robotId);
        return session != null && session.isOpen();
    }
}