package com.ruoyi.mode.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.mode.TestApplication;
import com.ruoyi.mode.config.TestSecurityConfig;
import com.ruoyi.mode.domain.SysRobot;
import com.ruoyi.mode.service.ISysRobotService;
import com.ruoyi.mode.utils.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = TestApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("机器人Controller测试")
@Import(TestSecurityConfig.class)  // 导入测试安全配置
@WithMockUser(username = "admin", authorities = {
        "system:robot:list",
        "system:robot:query",
        "system:robot:add",
        "system:robot:edit",
        "system:robot:remove"
})
class SysRobotControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ISysRobotService sysRobotService;

    private SysRobot testRobot;
    private List<SysRobot> testRobotList;

    @BeforeEach
    void setUp() {
        testRobot = TestDataBuilder.buildTestRobot(1L);
        testRobotList = TestDataBuilder.buildTestRobotList(5);
    }

    @Test
    @DisplayName("测试查询机器人列表 - 成功")
    void testList_Success() throws Exception {
        when(sysRobotService.selectSysRobotList(any(SysRobot.class))).thenReturn(testRobotList);

        mockMvc.perform(get("/system/robot/list")
                        .param("pageNum", "1")
                        .param("pageSize", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("测试获取机器人详情 - 成功")
    void testGetInfo_Success() throws Exception {
        when(sysRobotService.selectSysRobotById(1L)).thenReturn(testRobot);

        mockMvc.perform(get("/system/robot/{robotId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.robotId").value(1))
                .andExpect(jsonPath("$.data.robotName").value("测试机器人1"));
    }

    @Test
    @DisplayName("测试新增机器人 - 成功")
    void testAdd_Success() throws Exception {
        when(sysRobotService.insertSysRobot(any(SysRobot.class))).thenReturn(1);

        mockMvc.perform(post("/system/robot")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRobot)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(sysRobotService).insertSysRobot(any(SysRobot.class));
    }

    @Test
    @DisplayName("测试修改机器人 - 成功")
    void testEdit_Success() throws Exception {
        when(sysRobotService.updateSysRobot(any(SysRobot.class))).thenReturn(1);

        mockMvc.perform(put("/system/robot")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRobot)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(sysRobotService).updateSysRobot(any(SysRobot.class));
    }

    @Test
    @DisplayName("测试删除机器人 - 成功")
    void testRemove_Success() throws Exception {
        when(sysRobotService.deleteSysRobotByIds(any())).thenReturn(2);

        mockMvc.perform(delete("/system/robot/{robotIds}", "1,2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(sysRobotService).deleteSysRobotByIds(any());
    }

    @Test
    @DisplayName("测试更新机器人模式 - 成功")
    void testUpdateMode_Success() throws Exception {
        when(sysRobotService.updateRobotMode(eq(1L), eq(5L))).thenReturn(1);

        SysRobot modeUpdate = new SysRobot();
        modeUpdate.setRobotId(1L);
        modeUpdate.setCurrentMode(5L);

        mockMvc.perform(put("/system/robot/updateMode")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(modeUpdate)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(sysRobotService).updateRobotMode(1L, 5L);
    }

    @Test
    @DisplayName("测试批量重启机器人 - 成功")
    void testBatchRestart_Success() throws Exception {
        Long[] robotIds = {1L, 2L, 3L};
        when(sysRobotService.batchRestartAsync(any())).thenReturn(3);

        mockMvc.perform(put("/system/robot/batchRestart")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(robotIds)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("测试紧急停止机器人 - 成功")
    void testEmergencyStop_Success() throws Exception {
        Long[] robotIds = {1L, 2L};
        when(sysRobotService.emergencyStop(any())).thenReturn(2);

        mockMvc.perform(put("/system/robot/emergencyStop")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(robotIds)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("测试切换待机模式 - 成功")
    void testStandbyMode_Success() throws Exception {
        Long[] robotIds = {1L, 2L};
        when(sysRobotService.standbyMode(any())).thenReturn(2);

        mockMvc.perform(put("/system/robot/standbyMode")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(robotIds)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("测试切换维护模式 - 成功")
    void testMaintenanceMode_Success() throws Exception {
        Long[] robotIds = {1L, 2L};
        when(sysRobotService.maintenanceMode(any())).thenReturn(2);

        mockMvc.perform(put("/system/robot/maintenanceMode")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(robotIds)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("测试切换充电模式 - 成功")
    void testChargeMode_Success() throws Exception {
        Long[] robotIds = {1L, 2L};
        when(sysRobotService.chargeMode(any())).thenReturn(2);

        mockMvc.perform(put("/system/robot/chargeMode")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(robotIds)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}