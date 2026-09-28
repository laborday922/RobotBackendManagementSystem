package com.ruoyi.robots.service;
import com.ruoyi.common.core.websocket.RobotWebSocketMessage;
import com.ruoyi.robots.controller.dto.RobotStatusDto;
import com.ruoyi.robots.websocket.RobotWebSocketHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

@Service
@Slf4j
public class RobotHeartbeatService {
    @Autowired
    private RobotWebSocketHandler webSocketHandler;
    @Autowired
    private IRobotsService robotService;

    /** 心跳超时阈值（毫秒），超过该时长未收到机器人消息则判定为离线 */
    @Value("${robot.heartbeat.timeout:30000}")
    private long heartbeatTimeout;

    @Scheduled(fixedDelay = 10000) // 每30秒发送一次心跳
    public void sendHeartbeat() {
        var sessions = webSocketHandler.getRobotSessions();
        for (var entry : sessions.entrySet()) {
            Long robotId = entry.getKey();
            WebSocketSession session = entry.getValue();
            if (session.isOpen()) {
                try {
                    webSocketHandler.sendMessage(session, RobotWebSocketMessage.heartbeat());
                    System.out.println("发送心跳给机器人成功");
                } catch (IOException e) {
                    System.out.println("发送心跳给机器人失败");
                    log.warn("发送心跳给机器人 {} 失败", robotId, e);
                    // 心跳失败，标记离线
                    RobotStatusDto robot = new RobotStatusDto();
                    robot.setId(robotId);
                    robot.setStatus(0);
                    robot.setLastHeartbeatTime(new Date(0));
                    robotService.updateRobotStatus(robot);
                    try {
                        session.close();
                    } catch (IOException ex) {
                        System.out.println("关闭会话失败");
                        log.error("关闭会话失败", ex);
                    }
                }
            }
        }
    }

    /**
     * 心跳超时检测：对已连接但长时间未收到任何消息的机器人判定为离线并关闭会话。
     * 用于覆盖机器人被强制关机（未发送 CLOSE/FIN）导致服务端无法感知断开的场景。
     */
    @Scheduled(fixedDelay = 10000)
    public void checkHeartbeatTimeout() {
        long now = System.currentTimeMillis();
        for (Map.Entry<Long, WebSocketSession> entry : webSocketHandler.getRobotSessions().entrySet()) {
            Long robotId = entry.getKey();
            Long lastActive = webSocketHandler.getLastActiveTime().get(robotId);
            if (lastActive != null && now - lastActive > heartbeatTimeout) {
                log.warn("机器人 {} 心跳超时（{}ms 未收到消息），判定离线", robotId, now - lastActive);
                webSocketHandler.handleTimeoutOffline(robotId);
            }
        }
    }
}
