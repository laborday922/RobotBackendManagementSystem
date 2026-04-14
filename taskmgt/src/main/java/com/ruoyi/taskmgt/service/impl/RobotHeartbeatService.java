package com.ruoyi.taskmgt.service.impl;
import com.ruoyi.common.core.websocket.RobotWebSocketMessage;
import com.ruoyi.robots.controller.dto.RobotStatusDto;
import com.ruoyi.robots.service.IRobotsService;
import com.ruoyi.taskmgt.websocket.RobotWebSocketHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Date;

@Service
@Slf4j
public class RobotHeartbeatService {

    @Autowired
    private RobotWebSocketHandler webSocketHandler;
    @Autowired
    private IRobotsService robotService;

    @Scheduled(fixedDelay = 30000) // 每30秒发送一次心跳
    public void sendHeartbeat() {
        var sessions = webSocketHandler.getRobotSessions();
        for (var entry : sessions.entrySet()) {
            Long robotId = entry.getKey();
            WebSocketSession session = entry.getValue();
            if (session.isOpen()) {
                try {
                    webSocketHandler.sendMessage(session, RobotWebSocketMessage.heartbeat());
                } catch (IOException e) {
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
                        log.error("关闭会话失败", ex);
                    }
                }
            }
        }
    }
}
