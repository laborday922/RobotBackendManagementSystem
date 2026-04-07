package com.ruoyi.data.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.threadlocal.TenantContext;
import com.ruoyi.data.TestApplication;
import com.ruoyi.data.ai.service.AiAnalysisService;
import com.ruoyi.data.metric.domain.bo.MetricDefinition;
import com.ruoyi.data.metric.service.MetricDefinitionService;
import com.ruoyi.data.mock.WithMockRuoyiUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = TestApplication.class)
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@WithMockRuoyiUser(username = "admin", userId = "1", authorities = {"*:*:*"}, deptId = "103")
public class MetricComputeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RedisCache redisCache;

    @MockBean
    private AiAnalysisService aiAnalysisService;

    @Autowired
    private MetricDefinitionService metricDefinitionService;

    private Long testMetricId;

    @BeforeEach
    void setUp() {
        TenantContext.set(1L);
        MetricDefinition metric = new MetricDefinition();
        metric.setMetricName("测试计算指标");
        metric.setCategory("性能");
        metric.setDescription("用于测试计算的指标");
        metric.setDataSources(Arrays.asList("mysql", "api"));
        metric.setSelectedFields(Arrays.asList("field1", "field2"));
        metric.setCalculationExpression("sum(value)");
        metric.setUpdateFrequency("daily");
        metric.setChartType("line");
        metric.setEnableAlert(true);
        metric.setAlertThreshold(100.0);
        metric.setTags(Arrays.asList("test", "compute"));
        testMetricId = metricDefinitionService.create(metric);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void testCompute() throws Exception {
        mockMvc.perform(post("/metric/compute/{metricId}", 2L)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isNumber());
    }

    @Test
    void testChart() throws Exception {
        mockMvc.perform(get("/metric/chart/{metricId}", 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    void testCompute_NotFound() throws Exception {
        Long nonExistentId = 99999L;
        mockMvc.perform(post("/metric/compute/{metricId}", nonExistentId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }
}