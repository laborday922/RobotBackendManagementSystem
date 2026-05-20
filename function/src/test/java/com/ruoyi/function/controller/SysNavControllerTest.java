package com.ruoyi.function.controller;

import com.ruoyi.function.BaseControllerTest;
import com.ruoyi.function.TestApplication;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.function.controller.dto.request.NavigationRequest;
import com.ruoyi.function.domain.SysNavConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = TestApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("导航指引Controller测试")
class SysNavControllerTest extends BaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("获取导航配置 - 成功")
    void testGetNavConfig() throws Exception {
        mockMvc.perform(get("/func/nav/config")
                        .param("robotId", "robot_001")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("获取导航配置 - 机器人ID为空")
    void testGetNavConfigWithoutRobotId() throws Exception {
        mockMvc.perform(get("/func/nav/config")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    @DisplayName("保存导航配置 - 成功")
    void testSaveNavConfig() throws Exception {
        SysNavConfig config = new SysNavConfig();
        config.setRobotId("robot_001");
        config.setMapId(1L);
        config.setVoiceType("custom");
        config.setBeforeMsg("测试出发前播报");
        config.setDuringMsg("测试导航中播报");
        config.setAfterMsg("测试到达后播报");

        mockMvc.perform(post("/func/nav/config")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(config)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("保存导航配置 - 机器人ID为空")
    void testSaveNavConfigWithoutRobotId() throws Exception {
        SysNavConfig config = new SysNavConfig();
        config.setVoiceType("custom");

        mockMvc.perform(post("/func/nav/config")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(config)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    @DisplayName("保存导航配置 - 无效的播报类型")
    void testSaveNavConfigInvalidVoiceType() throws Exception {
        SysNavConfig config = new SysNavConfig();
        config.setRobotId("robot_001");
        config.setVoiceType("invalid_type");

        mockMvc.perform(post("/func/nav/config")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(config)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    @DisplayName("开始导航 - 成功")
    void testStartNavigation() throws Exception {
        NavigationRequest request = new NavigationRequest();
        request.setPointName("测试点位1");
        request.setVoiceEnabled(true);

        mockMvc.perform(post("/func/nav/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("开始导航 - 目标点位名称为空")
    void testStartNavigationWithoutPointName() throws Exception {
        NavigationRequest request = new NavigationRequest();
        request.setPointName("");

        mockMvc.perform(post("/func/nav/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("紧急停止 - 成功")
    void testEmergencyStop() throws Exception {
        mockMvc.perform(post("/func/nav/stop")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}