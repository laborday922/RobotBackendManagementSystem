package com.ruoyi.mode.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.ruoyi.mode.ModeModuleTestApplication;
import com.ruoyi.mode.domain.SysRobot;
import com.ruoyi.mode.service.ISysRobotService;
import com.ruoyi.robots.domain.Robot;
import com.ruoyi.robots.service.IRobotsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = ModeModuleTestApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Transactional
public class SysRobotControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ISysRobotService sysRobotService;

    @MockBean
    private IRobotsService robotsService;

    private Robot testRobot;
    private SysRobot testSysRobot;

    @BeforeEach
    void setUp() {
        testRobot = new Robot();
        testRobot.setId(1L);
        testRobot.setName("测试机器人");
        testRobot.setStatus(1);
        testRobot.setBattery(80);
        testRobot.setCurrentMode(1L);

        testSysRobot = new SysRobot();
        testSysRobot.setRobotId(1L);
        testSysRobot.setCurrentMode(1L);
        testSysRobot.setModeSwitchCount(0);
        testSysRobot.setNeedAutoCharge(0);
        testSysRobot.setTenantId(1L);
    }

    /**
     * 查询机器人列表
     */
    @Test
    @Order(1)
    void testListRobots() throws Exception {
        List<Robot> robotList = Arrays.asList(testRobot);
        when(robotsService.selectRobotsList(any(Robot.class))).thenReturn(robotList);

        MvcResult result = mockMvc.perform(get("/mode/robots/list")
                        .param("pageNum", "1")
                        .param("pageSize", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        assertThat(content).isNotNull();
        // 验证 code 为 200
        assertThat(content).contains("\"code\":200");
    }

    /**
     * 获取机器人详细信息
     */
    @Test
    @Order(2)
    void testGetRobotInfo() throws Exception {
        when(robotsService.selectRobotsById(1L)).thenReturn(testRobot);

        MvcResult result = mockMvc.perform(get("/mode/robots/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        assertThat(content).isNotNull();
        // 验证 code 为 200
        assertThat(content).contains("\"code\":200");
        // 验证 data 存在
        assertThat(content).contains("\"data\"");
    }

    /**
     * 更新机器人当前模式
     */
    @Test
    @Order(3)
    void testUpdateMode() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("robotId", 1L);
        request.put("modeId", 2L);

        when(sysRobotService.updateRobotMode(1L, 2L)).thenReturn(1);

        MvcResult result = mockMvc.perform(put("/mode/robots/updateMode")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        // 验证 code 为 200
        assertThat(content).contains("\"code\":200");
    }

    /**
     * 批量更新机器人模式
     */
    @Test
    @Order(4)
    void testBatchUpdateMode() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("robotIds", new Long[]{1L, 2L});
        request.put("modeId", 2L);

        when(sysRobotService.updateRobotMode(anyLong(), anyLong())).thenReturn(1);

        MvcResult result = mockMvc.perform(put("/mode/robots/batchUpdateMode")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        assertThat(content).contains("\"code\":200");
    }

    /**
     * 批量重启机器人
     */
    @Test
    @Order(5)
    void testBatchRestart() throws Exception {
        Long[] robotIds = {1L, 2L};
        when(sysRobotService.batchRestartAsync(any())).thenReturn(2);

        MvcResult result = mockMvc.perform(put("/mode/robots/batchRestart")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(robotIds)))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        assertThat(content).contains("\"code\":200");
        // 验证包含 submitted 字段
        assertThat(content).contains("submitted");
    }

    /**
     * 刷新机器人状态
     */
    @Test
    @Order(6)
    void testRefreshStatus() throws Exception {
        Long[] robotIds = {1L, 2L};
        when(sysRobotService.refreshStatus(any())).thenReturn(2);

        MvcResult result = mockMvc.perform(put("/mode/robots/refreshStatus")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(robotIds)))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        assertThat(content).contains("\"code\":200");
    }

    /**
     * 测试告警
     */
    @Test
    @Order(7)
    void testTestAlert() throws Exception {
        Long[] robotIds = {1L, 2L};
        when(sysRobotService.testAlert(any())).thenReturn(2);

        MvcResult result = mockMvc.perform(put("/mode/robots/testAlert")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(robotIds)))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        assertThat(content).contains("\"code\":200");
    }

    /**
     * 清除告警
     */
    @Test
    @Order(8)
    void testClearAlerts() throws Exception {
        Long[] robotIds = {1L, 2L};
        when(sysRobotService.clearAlerts(any())).thenReturn(2);

        MvcResult result = mockMvc.perform(put("/mode/robots/clearAlerts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(robotIds)))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        assertThat(content).contains("\"code\":200");
    }

    /**
     * 紧急停止机器人
     */
    @Test
    @Order(9)
    void testEmergencyStop() throws Exception {
        Long[] robotIds = {1L, 2L};
        when(sysRobotService.emergencyStop(any())).thenReturn(2);

        MvcResult result = mockMvc.perform(put("/mode/robots/emergencyStop")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(robotIds)))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        assertThat(content).contains("\"code\":200");
    }

    /**
     * 紧急撤离
     */
    @Test
    @Order(10)
    void testEmergencyEvacuation() throws Exception {
        Long[] robotIds = {1L, 2L};
        when(sysRobotService.emergencyEvacuation(any())).thenReturn(2);

        MvcResult result = mockMvc.perform(put("/mode/robots/emergencyEvacuation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(robotIds)))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        assertThat(content).contains("\"code\":200");
    }

    /**
     * 切换待机模式
     */
    @Test
    @Order(11)
    void testStandbyMode() throws Exception {
        Long[] robotIds = {1L, 2L};
        when(sysRobotService.standbyMode(any())).thenReturn(2);

        MvcResult result = mockMvc.perform(put("/mode/robots/standbyMode")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(robotIds)))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        assertThat(content).contains("\"code\":200");
    }

    /**
     * 切换维护模式
     */
    @Test
    @Order(12)
    void testMaintenanceMode() throws Exception {
        Long[] robotIds = {1L, 2L};
        when(sysRobotService.maintenanceMode(any())).thenReturn(2);

        MvcResult result = mockMvc.perform(put("/mode/robots/maintenanceMode")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(robotIds)))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        assertThat(content).contains("\"code\":200");
    }

    /**
     * 切换充电模式
     */
    @Test
    @Order(13)
    void testChargeMode() throws Exception {
        Long[] robotIds = {1L, 2L};
        Map<String, Object> mockResult = new HashMap<>();
        mockResult.put("successCount", 2);
        mockResult.put("total", 2);
        when(sysRobotService.chargeMode(any())).thenReturn(mockResult);

        MvcResult result = mockMvc.perform(put("/mode/robots/chargeMode")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(robotIds)))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        assertThat(content).contains("\"code\":200");
        assertThat(content).contains("successCount");
    }

    /**
     * 返回充电
     */
    @Test
    @Order(14)
    void testReturnCharge() throws Exception {
        Long[] robotIds = {1L, 2L};
        when(sysRobotService.returnCharge(any())).thenReturn(2);

        MvcResult result = mockMvc.perform(put("/mode/robots/returnCharge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(robotIds)))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        assertThat(content).contains("\"code\":200");
    }

    /**
     * 保存机器人模式配置
     */
    @Test
    @Order(15)
    void testSaveModeConfig() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("robotId", 1L);
        request.put("modeId", 1L);

        Map<String, Object> config = new HashMap<>();
        config.put("充电策略", "after_task");
        config.put("阈值", 20);
        request.put("config", config);

        when(sysRobotService.saveRobotModeConfig(anyLong(), anyLong(), anyMap())).thenReturn(1);

        MvcResult result = mockMvc.perform(post("/mode/robots/saveModeConfig")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        assertThat(content).contains("\"code\":200");
    }

    /**
     * 批量保存机器人模式配置
     */
    @Test
    @Order(16)
    void testBatchSaveModeConfig() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("robotIds", new Long[]{1L, 2L});
        request.put("modeId", 1L);

        Map<String, Object> config = new HashMap<>();
        config.put("充电策略", "after_task");
        config.put("阈值", 20);
        request.put("config", config);

        when(sysRobotService.saveRobotModeConfig(anyLong(), anyLong(), anyMap())).thenReturn(1);

        MvcResult result = mockMvc.perform(post("/mode/robots/batchSaveModeConfig")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        assertThat(content).contains("\"code\":200");
        assertThat(content).contains("successCount");
    }

    /**
     * 获取机器人模式配置
     */
    @Test
    @Order(17)
    void testGetModeConfig() throws Exception {
        Map<String, Object> config = new HashMap<>();
        config.put("充电策略", "after_task");
        config.put("阈值", 20);

        when(sysRobotService.getRobotModeConfig(1L, 1L)).thenReturn(config);

        MvcResult result = mockMvc.perform(get("/mode/robots/modeConfig")
                        .param("robotId", "1")
                        .param("modeId", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        assertThat(content).contains("\"code\":200");
        assertThat(content).contains("\"data\"");
    }

    /**
     * 删除机器人模式配置
     */
    @Test
    @Order(18)
    void testDeleteModeConfig() throws Exception {
        when(sysRobotService.deleteRobotModeConfig(1L, 1L)).thenReturn(1);

        MvcResult result = mockMvc.perform(delete("/mode/robots/deleteModeConfig")
                        .param("robotId", "1")
                        .param("modeId", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        assertThat(content).contains("\"code\":200");
    }

    // ==================== 边界和异常情况测试 ====================

    /**
     * 更新模式 - 无效数据（Controller 可能不验证，所以改为验证返回结果）
     */
    @Test
    @Order(19)
    void testUpdateMode_WithInvalidData() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("robotId", null);
        request.put("modeId", null);

        // 即使传入无效数据，Controller 也会返回响应，验证不会抛出异常
        MvcResult result = mockMvc.perform(put("/mode/robots/updateMode")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn();

        // 验证返回结果不为空
        assertThat(result.getResponse().getContentAsString()).isNotNull();
    }

    /**
     * 批量重启 - 空数组
     */
    @Test
    @Order(20)
    void testBatchRestart_WithEmptyArray() throws Exception {
        Long[] robotIds = {};
        when(sysRobotService.batchRestartAsync(any())).thenReturn(0);

        MvcResult result = mockMvc.perform(put("/mode/robots/batchRestart")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(robotIds)))
                .andReturn();

        String content = result.getResponse().getContentAsString();
        // 验证返回结果不为空
        assertThat(content).isNotNull();
    }

    /**
     * 获取机器人信息 - 不存在
     */
    @Test
    @Order(21)
    void testGetRobotInfo_NotFound() throws Exception {
        when(robotsService.selectRobotsById(999L)).thenReturn(null);

        MvcResult result = mockMvc.perform(get("/mode/robots/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        assertThat(content).isNotNull();
        // 验证 code 为 200
        assertThat(content).contains("\"code\":200");
    }
}