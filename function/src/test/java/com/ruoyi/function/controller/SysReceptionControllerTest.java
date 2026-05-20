package com.ruoyi.function.controller;

import com.ruoyi.function.BaseControllerTest;
import com.ruoyi.function.TestApplication;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.function.domain.SysReceptionConfig;
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
@DisplayName("业务接待配置Controller测试")
class SysReceptionControllerTest extends BaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("获取配置 - 成功")
    void testGetConfig() throws Exception {
        mockMvc.perform(get("/func/reception/config/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("保存配置 - 成功")
    void testSaveConfig() throws Exception {
        SysReceptionConfig config = new SysReceptionConfig();
        config.setRobotId(1L);
        config.setWelcome("测试欢迎语");
        config.setRepeat("测试重复语");
        config.setIdle("测试未唤醒语");
        config.setVipEnabled("1");
        config.setVipGreeting("测试VIP语");
        config.setVipMulti("测试多VIP语");
        config.setStrangerEnabled("1");
        config.setStrangerGreeting("测试陌生人语");
        config.setStrangerMulti("测试多陌生人语");

        mockMvc.perform(post("/func/reception/config")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(config)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("重置为默认配置 - 成功")
    void testResetConfig() throws Exception {
        mockMvc.perform(put("/func/reception/reset/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}