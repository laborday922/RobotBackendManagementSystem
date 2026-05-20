package com.ruoyi.mode.config;

import com.ruoyi.mode.invoker.ModeSwitchInvoker;
import com.ruoyi.robots.websocket.RobotWebSocketHandler;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class ModeSwitchInvokerMockConfig {

    @Bean
    @Primary
    public ModeSwitchInvoker modeSwitchInvoker() {
        RobotWebSocketHandler mockWebSocketHandler = mock(RobotWebSocketHandler.class);
        ApplicationEventPublisher mockEventPublisher = mock(ApplicationEventPublisher.class);
        return new ModeSwitchInvoker(mockWebSocketHandler, mockEventPublisher);
    }
}