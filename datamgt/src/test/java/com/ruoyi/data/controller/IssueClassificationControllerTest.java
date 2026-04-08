package com.ruoyi.data.controller;

import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.threadlocal.TenantContext;
import com.ruoyi.data.TestApplication;
import com.ruoyi.data.ai.service.AiAnalysisService;
import com.ruoyi.data.ai.service.TongYiService;
import com.ruoyi.data.mock.WithMockRuoyiUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = TestApplication.class)
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@WithMockRuoyiUser(username = "admin", userId = "1", authorities = {"*:*:*"}, deptId = "103")
public class IssueClassificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RedisCache redisCache;

    @MockBean
    private AiAnalysisService aiAnalysisService;

    @MockBean
    private TongYiService tongYiService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        TenantContext.set(1L);
        jdbcTemplate.execute("INSERT INTO t_interaction_history " +
                "(interaction_id, interaction_type, interaction_content, interaction_time, status, evaluation_text, tenant_id, task_id) VALUES " +
                "(1, 1, '屏幕不亮', NOW(), 1, '差', 1, 1), " +
                "(2, 1, '按键失灵', NOW(), 1, '一般', 1, 1), " +
                "(3, 2, '闪退', NOW(), 1, '严重', 1, 1)");
    }
    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void testGetDistribution() throws Exception {
        mockMvc.perform(get("/ai/issue-classification/distribution")
                        .param("time_range", "last7days"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void testGetTrend() throws Exception {
        mockMvc.perform(get("/ai/issue-classification/trend")
                        .param("category", "硬件故障")
                        .param("granularity", "day")
                        .param("time_range", "last7days"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}