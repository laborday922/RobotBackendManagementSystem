package com.ruoyi.mode.service.impl;

import com.ruoyi.mode.domain.SysRobot;
import com.ruoyi.mode.mapper.SysRobotMapper;
import com.ruoyi.mode.service.ISysRobotOperationService;
import com.ruoyi.mode.utils.TestDataBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("机器人Service测试")
class SysRobotServiceImplTest {

    @Mock
    private SysRobotMapper sysRobotMapper;

    @Mock
    private ISysRobotOperationService robotOperationService;

    @InjectMocks
    private SysRobotServiceImpl sysRobotService;

    private SysRobot testRobot;
    private Long[] testRobotIds;

    @BeforeEach
    void setUp() {
        testRobot = TestDataBuilder.buildTestRobot(1L);
        testRobotIds = new Long[]{1L};

        // 关键修复：设置 SecurityContext
        setupSecurityContext();
    }

    @AfterEach
    void tearDown() {
        // 清理 SecurityContext
        SecurityContextHolder.clearContext();
    }

    /**
     * 设置安全上下文，模拟登录用户
     */
    private void setupSecurityContext() {
        // 创建用户权限
        List<SimpleGrantedAuthority> authorities = Arrays.asList(
                new SimpleGrantedAuthority("ROLE_ADMIN"),
                new SimpleGrantedAuthority("system:robot:edit")
        );

        // 创建用户详情
        UserDetails userDetails = new User("admin", "password", authorities);

        // 创建认证令牌
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                "password",
                authorities
        );

        // 设置到 SecurityContext
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    @DisplayName("测试根据ID查询机器人 - 成功")
    void testSelectSysRobotById_Success() {
        when(sysRobotMapper.selectSysRobotById(1L)).thenReturn(testRobot);

        SysRobot result = sysRobotService.selectSysRobotById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getRobotId()).isEqualTo(1L);
        verify(sysRobotMapper).selectSysRobotById(1L);
    }

    @Test
    @DisplayName("测试查询机器人列表")
    void testSelectSysRobotList() {
        List<SysRobot> mockList = TestDataBuilder.buildTestRobotList(5);
        when(sysRobotMapper.selectSysRobotList(any(SysRobot.class))).thenReturn(mockList);

        SysRobot query = new SysRobot();
        query.setStatus(1);

        List<SysRobot> result = sysRobotService.selectSysRobotList(query);

        assertThat(result).hasSize(5);
        verify(sysRobotMapper).selectSysRobotList(query);
    }

    @Test
    @DisplayName("测试更新机器人模式 - 成功")
    void testUpdateRobotMode_Success() {
        when(sysRobotMapper.selectSysRobotById(1L)).thenReturn(testRobot);
        when(sysRobotMapper.updateRobotMode(1L, 5L)).thenReturn(1);
        when(robotOperationService.recordOperation(anyLong(), anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(1);

        int result = sysRobotService.updateRobotMode(1L, 5L);

        assertThat(result).isEqualTo(1);
        verify(sysRobotMapper).updateRobotMode(1L, 5L);
    }

    @Test
    @DisplayName("测试更新机器人模式 - 机器人不存在")
    void testUpdateRobotMode_RobotNotFound() {
        when(sysRobotMapper.selectSysRobotById(999L)).thenReturn(null);

        int result = sysRobotService.updateRobotMode(999L, 5L);

        assertThat(result).isEqualTo(0);
        verify(sysRobotMapper, never()).updateRobotMode(anyLong(), anyLong());
    }

    @Test
    @DisplayName("测试紧急停止机器人")
    void testEmergencyStop() {
        when(sysRobotMapper.selectSysRobotById(1L)).thenReturn(testRobot);
        when(sysRobotMapper.updateSysRobot(any(SysRobot.class))).thenReturn(1);
        when(robotOperationService.recordOperation(anyLong(), anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(1);

        int result = sysRobotService.emergencyStop(testRobotIds);

        assertThat(result).isEqualTo(1);
        verify(sysRobotMapper, atLeastOnce()).updateSysRobot(any(SysRobot.class));
    }

    @Test
    @DisplayName("测试切换待机模式")
    void testStandbyMode() {
        when(sysRobotMapper.selectSysRobotById(1L)).thenReturn(testRobot);
        when(sysRobotMapper.updateSysRobot(any(SysRobot.class))).thenReturn(1);
        when(robotOperationService.recordOperation(anyLong(), anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(1);

        int result = sysRobotService.standbyMode(testRobotIds);

        assertThat(result).isEqualTo(1);
    }

    @Test
    @DisplayName("测试查询在线机器人")
    void testSelectOnlineRobots() {
        List<SysRobot> onlineRobots = TestDataBuilder.buildTestRobotList(3);
        onlineRobots.forEach(r -> r.setStatus(1));

        when(sysRobotMapper.selectSysRobotList(any(SysRobot.class))).thenReturn(onlineRobots);

        List<SysRobot> result = sysRobotService.selectOnlineRobots();

        assertThat(result).isNotEmpty();
        assertThat(result).allMatch(r -> r.getStatus() == 1);
    }

    @Test
    @DisplayName("测试查询低电量机器人")
    void testSelectLowBatteryRobots() {
        List<SysRobot> lowBatteryRobots = TestDataBuilder.buildTestRobotList(2);
        lowBatteryRobots.forEach(r -> r.setBattery(15L));

        when(sysRobotMapper.selectLowBatteryRobots(20)).thenReturn(lowBatteryRobots);

        List<SysRobot> result = sysRobotService.selectLowBatteryRobots(20);

        assertThat(result).isNotEmpty();
        assertThat(result).allMatch(r -> r.getBattery() <= 20);
    }
}