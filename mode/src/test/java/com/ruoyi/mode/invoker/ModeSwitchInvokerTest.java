package com.ruoyi.mode.invoker;

import com.ruoyi.mode.ModeModuleTestApplication;
import com.ruoyi.mode.config.TestMockConfig;
import com.ruoyi.mode.invoker.dto.ModeSwitchResponse;
import com.ruoyi.robots.websocket.RobotWebSocketHandler;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = ModeModuleTestApplication.class)
@Import(TestMockConfig.class)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ModeSwitchInvokerTest {

    @Autowired
    private ModeSwitchInvoker modeSwitchInvoker;

    @MockBean
    private RobotWebSocketHandler webSocketHandler;

    @Test
    @Order(1)
    void testIsRobotOnline() {
        when(webSocketHandler.isOnline(anyLong())).thenReturn(true);
        boolean isOnline = modeSwitchInvoker.isRobotOnline(1L);
        assertThat(isOnline).isTrue();
    }

    @Test
    @Order(2)
    void testSwitchModeSync_OfflineRobot() {
        when(webSocketHandler.isOnline(anyLong())).thenReturn(false);
        ModeSwitchResponse response = modeSwitchInvoker.switchModeSync(1L, 2L, "维护模式", 10);
        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getErrorMsg()).contains("不在线");
    }

    @Test
    @Order(3)
    void testSwitchModeAsync_OfflineRobot() {
        when(webSocketHandler.isOnline(anyLong())).thenReturn(false);
        String traceId = modeSwitchInvoker.switchModeAsync(1L, 2L, "维护模式", null);
        assertThat(traceId).isNull();
    }

    @Test
    @Order(4)
    void testSwitchModeAsync_OnlineRobot() {
        when(webSocketHandler.isOnline(anyLong())).thenReturn(true);
        String traceId = modeSwitchInvoker.switchModeAsync(1L, 2L, "维护模式", "http://localhost:8080/callback");
        assertThat(traceId).isNotNull();
    }

    @Test
    @Order(5)
    void testQuerySwitchStatus() {
        when(webSocketHandler.isOnline(anyLong())).thenReturn(true);
        ModeSwitchResponse response = modeSwitchInvoker.querySwitchStatus(1L, "test-trace-id");
        assertThat(response).isNotNull();
    }
}