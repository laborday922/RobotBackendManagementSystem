package com.ruoyi.mode.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.mode.ModeModuleTestApplication;
import com.ruoyi.mode.domain.SysModeHistory;
import com.ruoyi.mode.service.ISysModeHistoryService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = ModeModuleTestApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Transactional
public class SysModeHistoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ISysModeHistoryService historyService;

    private SysModeHistory testHistory;
    private Long testHistoryId;

    @BeforeEach
    void setUp() {
        testHistory = new SysModeHistory();
        testHistory.setOperationTime(new Date());
        testHistory.setOperationType("mode-switch");
        testHistory.setRobotId(1L);
        testHistory.setRobotName("测试机器人");
        testHistory.setModeId(1L);
        testHistory.setModeName("待机模式");
        testHistory.setContent("切换到待机模式");
        testHistory.setOperator("test_user");
        testHistory.setStatus("success");

        historyService.insertSysModeHistory(testHistory);
        testHistoryId = testHistory.getHistoryId();
    }

    @Test
    @Order(1)
    void testListHistory() throws Exception {
        mockMvc.perform(get("/system/history/list")
                        .param("pageNum", "1")
                        .param("pageSize", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @Order(2)
    void testGetInfo() throws Exception {
        mockMvc.perform(get("/system/history/{historyId}", testHistoryId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.operationType").value("mode-switch"));
    }

    @Test
    @Order(3)
    void testAddHistory() throws Exception {
        SysModeHistory newHistory = new SysModeHistory();
        newHistory.setOperationTime(new Date());
        newHistory.setOperationType("emergency");
        newHistory.setRobotId(2L);
        newHistory.setRobotName("测试机器人2");
        newHistory.setContent("紧急停止");
        newHistory.setOperator("test_user");
        newHistory.setStatus("success");

        mockMvc.perform(post("/system/history")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newHistory)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @Order(4)
    void testGetOperationTypeStats() throws Exception {
        mockMvc.perform(get("/system/history/stats/operationType")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @Order(5)
    void testGetRobotOperationStats() throws Exception {
        mockMvc.perform(get("/system/history/stats/robot")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @Order(6)
    void testGetDailyOperationStats() throws Exception {
        mockMvc.perform(get("/system/history/stats/daily")
                        .param("days", "7")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @Order(7)
    void testRemove() throws Exception {
        mockMvc.perform(delete("/system/history/{historyIds}", testHistoryId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}