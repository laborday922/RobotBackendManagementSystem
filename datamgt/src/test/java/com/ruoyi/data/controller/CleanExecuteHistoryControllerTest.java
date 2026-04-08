package com.ruoyi.data.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.data.TestApplication;
import com.ruoyi.data.clean.domain.CleanExecuteHistory;
import com.ruoyi.data.mock.WithMockRuoyiUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = TestApplication.class)
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@WithMockRuoyiUser(username = "admin", userId = "1", authorities = {"*:*:*"}, deptId = "103")
public class CleanExecuteHistoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // 防止 Redis 报错
    @MockBean
    private RedisCache redisCache;

    /**
     * 新增 + 查询（完整链路测试）
     */
    @Test
    public void testCreateAndGet() throws Exception {

        CleanExecuteHistory history = new CleanExecuteHistory();
        history.setStatus(1);

        // 1️⃣ 创建
        String createRes = mockMvc.perform(post("/clean/history/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(history)).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // 2️⃣ 解析返回ID
        Long id = objectMapper.readTree(createRes).get("data").asLong();

        // 3️⃣ 查询
        mockMvc.perform(get("/clean/history/{id}", id).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(id));
    }

    /**
     * 更新状态
     */
    @Test
    public void testUpdateStatus() throws Exception {

        CleanExecuteHistory history = new CleanExecuteHistory();
        history.setStatus(0);

        String res = mockMvc.perform(post("/clean/history/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(history)).with(csrf()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long id = objectMapper.readTree(res).get("data").asLong();

        mockMvc.perform(put("/clean/history/{id}/status", id)
                        .param("status", "1").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("更新状态成功"));
    }

    /**
     * 删除
     */
    @Test
    public void testDelete() throws Exception {

        CleanExecuteHistory history = new CleanExecuteHistory();
        history.setStatus(0);           // 设置状态

        String res = mockMvc.perform(post("/clean/history/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(history))
                        .with(csrf()))   // 携带 CSRF Token
                .andReturn()
                .getResponse()
                .getContentAsString();

        System.out.println("Response: " + res);

        Long id = objectMapper.readTree(res).get("data").asLong();

        mockMvc.perform(delete("/clean/history/{id}", id).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("删除成功"));
    }
}