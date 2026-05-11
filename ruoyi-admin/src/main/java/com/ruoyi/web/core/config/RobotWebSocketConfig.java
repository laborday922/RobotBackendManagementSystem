package com.ruoyi.web.core.config;

import com.ruoyi.robots.websocket.RobotWebSocketHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@Slf4j
public class RobotWebSocketConfig implements WebSocketConfigurer {

    private final RobotWebSocketHandler robotWebSocketHandler;

    public RobotWebSocketConfig(RobotWebSocketHandler robotWebSocketHandler) {
        log.info("注册 WebSocket 处理器，路径：/ws/robot");
        this.robotWebSocketHandler = robotWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(robotWebSocketHandler, "/ws/robot")
                .setAllowedOrigins("*");
    }
}