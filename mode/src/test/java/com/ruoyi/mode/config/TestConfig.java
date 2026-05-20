package com.ruoyi.mode.config;

import com.ruoyi.common.threadlocal.TenantContext;
import com.ruoyi.robots.mapper.RobotsMapper;
import com.ruoyi.robots.service.IRobotsService;
import com.ruoyi.taskmgt.websocket.RobotWebSocketHandler;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.annotation.PostConstruct;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;

/**
 * 测试配置类 - 移除 framework 依赖
 */
@TestConfiguration
public class TestConfig {

    @PostConstruct
    public void initTestContext() {
        // 设置测试租户上下文
        TenantContext.set(1L);

        // 设置安全上下文
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        Authentication authentication = new UsernamePasswordAuthenticationToken("test_user", "password");
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    /**
     * Mock RobotWebSocketHandler，避免WebSocket依赖
     */
    @Bean
    @Primary
    public RobotWebSocketHandler mockRobotWebSocketHandler() {
        RobotWebSocketHandler mock = Mockito.mock(RobotWebSocketHandler.class);
        Mockito.when(mock.isOnline(anyLong())).thenReturn(true);
        return mock;
    }

    /**
     * Mock IRobotsService
     */
    @Bean
    @Primary
    public IRobotsService mockRobotsService() {
        return Mockito.mock(IRobotsService.class);
    }

    /**
     * Mock RobotsMapper
     */
    @Bean
    @Primary
    public RobotsMapper mockRobotsMapper() {
        return Mockito.mock(RobotsMapper.class);
    }

    // 移除 TokenService 的 Mock，因为模式模块不需要它
    // 如果需要，可以创建一个简单的替代类
}