package com.ruoyi.data.controller;

import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.data.TestApplication;
import com.ruoyi.data.ai.service.AiAnalysisService;
import com.ruoyi.data.mock.WithMockRuoyiUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = TestApplication.class)
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@WithMockRuoyiUser(username = "admin", userId = "1", authorities = {"*:*:*"}, deptId = "103")
public class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // 若项目中使用了 Redis，需要 mock 掉；否则可删除
    @MockBean
    private RedisCache redisCache;

    @MockBean
    private AiAnalysisService aiAnalysisService;

//     ==================== 接口测试 ====================

    @Test
    void testGetFeedbackWordCloud() throws Exception {
        mockMvc.perform(get("/dashboard/feedback/wordcloud")
                        .param("start_time", "2026-01-01 00:00:00")
                        .param("end_time", "2026-12-31 23:59:59")
                        .param("feedback_type", "positive")
                        .with(csrf()))   // GET 请求通常不需要 csrf，但加也无妨
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void testGetRobotGeoDistribution() throws Exception {
        mockMvc.perform(get("/dashboard/service/robots/geo").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void testGetServiceOverview() throws Exception {
        mockMvc.perform(get("/dashboard/service/overview").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    void testGetTaskExecutions() throws Exception {
        mockMvc.perform(get("/dashboard/tasks/executions")
                        .param("limit", "20")
                        .param("offset", "0")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    void testGetRobotGroups() throws Exception {
        mockMvc.perform(get("/dashboard/robot/groups").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void testGetAnomalyTrend() throws Exception {
        mockMvc.perform(get("/dashboard/anomaly/trend")
                        .param("granularity", "day")
                        .param("time_range", "last7days")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").exists());
    }
}