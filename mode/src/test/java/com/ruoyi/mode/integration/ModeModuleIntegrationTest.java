package com.ruoyi.mode.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.mode.TestApplication;
import com.ruoyi.mode.config.TestSecurityConfig;
import com.ruoyi.mode.domain.SysMode;
import com.ruoyi.mode.domain.SysRobot;
import com.ruoyi.mode.mapper.SysModeMapper;
import com.ruoyi.mode.mapper.SysRobotMapper;
import com.ruoyi.mode.utils.TestDataBuilder;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;  // 添加这一行！解决 List 相关的错误

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = TestApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("模式模块集成测试")
@Import(TestSecurityConfig.class)
@WithMockUser(username = "admin", authorities = {
        "system:mode:list",
        "system:mode:query",
        "system:mode:add",
        "system:mode:edit",
        "system:mode:remove",
        "system:robot:list",
        "system:robot:query",
        "system:robot:add",
        "system:robot:edit",
        "system:robot:remove"
})
class ModeModuleIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SysModeMapper sysModeMapper;

    @Autowired
    private SysRobotMapper sysRobotMapper;

    private SysMode testMode;
    private SysRobot testRobot;

    @BeforeEach
    void setUp() {
        testMode = TestDataBuilder.buildTestMode(null);
        testRobot = TestDataBuilder.buildTestRobot(null);
    }

    @Test
    @Order(1)
    @DisplayName("完整流程测试：创建模式 -> 查询模式 -> 修改模式 -> 删除模式")
    void testCompleteModeFlow() throws Exception {
        // 1. 创建模式
        mockMvc.perform(post("/system/mode")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testMode)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 2. 查询模式列表
        mockMvc.perform(get("/system/mode/list")
                        .param("pageNum", "1")
                        .param("pageSize", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());

        // 3. 获取创建的模式ID（从数据库查询最新插入的记录）
        SysMode query = new SysMode();
        query.setModeName(testMode.getModeName());
        List<SysMode> modes = sysModeMapper.selectSysModeList(query);

        if (modes != null && !modes.isEmpty()) {
            SysMode savedMode = modes.get(0);
            savedMode.setModeName("集成测试修改后的模式");

            // 4. 修改模式
            mockMvc.perform(put("/system/mode")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(savedMode)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            // 5. 删除模式
            mockMvc.perform(delete("/system/mode/{modeIds}", savedMode.getModeId())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }

    @Test
    @Order(2)
    @DisplayName("完整流程测试：创建机器人 -> 切换模式 -> 批量操作")
    void testCompleteRobotFlow() throws Exception {
        // 1. 创建机器人
        mockMvc.perform(post("/system/robot")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRobot)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 2. 查询机器人列表
        mockMvc.perform(get("/system/robot/list")
                        .param("pageNum", "1")
                        .param("pageSize", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());

        // 3. 获取创建的机器人ID
        SysRobot query = new SysRobot();
        query.setRobotCode(testRobot.getRobotCode());
        List<SysRobot> robots = sysRobotMapper.selectSysRobotList(query);

        if (robots != null && !robots.isEmpty()) {
            SysRobot savedRobot = robots.get(0);
            Long[] robotIds = {savedRobot.getRobotId()};

            // 4. 切换待机模式
            mockMvc.perform(put("/system/robot/standbyMode")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(robotIds)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            // 5. 刷新状态
            mockMvc.perform(put("/system/robot/refreshStatus")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(robotIds)))
                    .andDo(print())
                    .andExpect(status().isOk());

            // 6. 删除机器人
            mockMvc.perform(delete("/system/robot/{robotIds}", savedRobot.getRobotId())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }
}