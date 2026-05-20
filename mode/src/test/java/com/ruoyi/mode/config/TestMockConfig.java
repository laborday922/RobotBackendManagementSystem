package com.ruoyi.mode.config;

import com.ruoyi.mode.invoker.ModeSwitchInvoker;
import com.ruoyi.robots.websocket.RobotWebSocketHandler;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@TestConfiguration
public class TestMockConfig {

    /**
     * Mock RobotWebSocketHandler，避免WebSocket依赖
     */
    @Bean
    @Primary
    public RobotWebSocketHandler robotWebSocketHandler() {
        RobotWebSocketHandler mock = mock(RobotWebSocketHandler.class);
        // 设置默认行为
        when(mock.isOnline(anyLong())).thenReturn(true);
        return mock;
    }
}