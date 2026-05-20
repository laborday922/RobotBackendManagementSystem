package com.ruoyi.mode.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.mode.ModeModuleTestApplication;
import com.ruoyi.mode.domain.SysMode;
import com.ruoyi.mode.service.ISysModeService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = ModeModuleTestApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Transactional
public class SysModeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ISysModeService sysModeService;

    private SysMode testMode;
    private Long testModeId;

    @BeforeEach
    void setUp() {
        testMode = new SysMode();
        testMode.setModeName("控制器测试模式");
        testMode.setModeType("custom");
        testMode.setModeColor("#FF5733");
        testMode.setModeIcon("fa fa-test");
        testMode.setDescription("控制器层测试模式");
        testMode.setEnabled("1");
        testMode.setOrderNum(300);

        sysModeService.insertSysMode(testMode);
        testModeId = testMode.getModeId();
    }

    @Test
    @Order(1)
    void testListMode() throws Exception {
        mockMvc.perform(get("/system/mode/list")
                        .param("pageNum", "1")
                        .param("pageSize", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @Order(2)
    void testGetInfo() throws Exception {
        mockMvc.perform(get("/system/mode/{modeId}", testModeId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.modeName").value("控制器测试模式"));
    }

    @Test
    @Order(3)
    void testAdd() throws Exception {
        SysMode newMode = new SysMode();
        newMode.setModeName("新增测试模式");
        newMode.setModeType("custom");
        newMode.setModeColor("#1890FF");
        newMode.setModeIcon("fa fa-new");
        newMode.setDescription("新增测试");
        newMode.setEnabled("1");
        newMode.setOrderNum(400);

        mockMvc.perform(post("/system/mode")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newMode)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @Order(4)
    void testEdit() throws Exception {
        testMode.setModeName("修改后的控制器测试模式");
        testMode.setDescription("修改后的描述");

        mockMvc.perform(put("/system/mode")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testMode)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @Order(5)
    void testChangeStatus() throws Exception {
        SysMode statusUpdate = new SysMode();
        statusUpdate.setModeId(testModeId);
        statusUpdate.setEnabled("0");

        mockMvc.perform(put("/system/mode/changeStatus")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @Order(6)
    void testRemove() throws Exception {
        mockMvc.perform(delete("/system/mode/{modeIds}", testModeId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}