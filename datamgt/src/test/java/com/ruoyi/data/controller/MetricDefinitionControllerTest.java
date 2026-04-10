package com.ruoyi.data.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.data.TestApplication;
import com.ruoyi.data.ai.service.AiAnalysisService;
import com.ruoyi.data.metric.domain.bo.MetricDefinition;
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

import java.util.Arrays;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = TestApplication.class)
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@WithMockRuoyiUser(username = "admin", userId = "1", authorities = {"*:*:*"}, deptId = "103")
public class MetricDefinitionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RedisCache redisCache;

    @MockBean
    private AiAnalysisService aiAnalysisService;

    // 创建一个带必要字段的 MetricDefinition 对象
    private MetricDefinition createValidMetricDefinition() {
        MetricDefinition metric = new MetricDefinition();
        metric.setMetricName("测试指标");
        metric.setCategory("性能");
        metric.setDescription("这是一个测试指标");

        // List<String> 类型字段
        List<String> dataSources = Arrays.asList("mysql", "api");
        metric.setDataSources(dataSources);
        List<String> selectedFields = Arrays.asList("field1", "field2");
        metric.setSelectedFields(selectedFields);
        List<String> tags = Arrays.asList("test", "demo");
        metric.setTags(tags);

        metric.setCalculationExpression("sum(value)");
        metric.setUpdateFrequency("daily");
        metric.setChartType("line");

        // Boolean 类型
        metric.setEnableAlert(true);   // 或 Boolean.TRUE

        // Double 类型（示例阈值）
        metric.setAlertThreshold(100.0);

        return metric;
    }


    @Test
    void testCreate() throws Exception {
        MetricDefinition metric = createValidMetricDefinition();

        mockMvc.perform(post("/metric/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(metric))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void testGetById() throws Exception {
        // 先创建一条记录
        MetricDefinition metric = createValidMetricDefinition();
        metric.setMetricName("查询测试指标");

        String createRes = mockMvc.perform(post("/metric/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(metric))
                        .with(csrf()))
                .andReturn()
                .getResponse()
                .getContentAsString();

//        Long id = objectMapper.readTree(createRes).get("data").asLong();

        Long id = 1L;
        mockMvc.perform(get("/metric/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void testListAll() throws Exception {
        mockMvc.perform(get("/metric/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void testUpdate() throws Exception {
        // 创建一条记录
        MetricDefinition metric = createValidMetricDefinition();
        metric.setMetricName("更新前指标");

        String createRes = mockMvc.perform(post("/metric/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(metric))
                        .with(csrf()))
                .andReturn()
                .getResponse()
                .getContentAsString();

//        Long id = objectMapper.readTree(createRes).get("data").asLong();
        Long id = 2L;
        // 修改记录
        metric.setId(id);
        metric.setMetricName("更新后指标");
        metric.setDescription("描述已被更新");

        mockMvc.perform(put("/metric/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(metric))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 验证更新结果
        mockMvc.perform(get("/metric/{id}", id))
                .andExpect(jsonPath("$.data.metricName").value("更新后指标"))
                .andExpect(jsonPath("$.data.description").value("描述已被更新"));
    }

    @Test
    void testDelete() throws Exception {
        // 创建一条记录
        MetricDefinition metric = createValidMetricDefinition();
        metric.setMetricName("待删除指标");

        String createRes = mockMvc.perform(post("/metric/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(metric))
                        .with(csrf()))
                .andReturn()
                .getResponse()
                .getContentAsString();

//        Long id = objectMapper.readTree(createRes).get("data").asLong();
        Long id = 1L;
        // 删除
        mockMvc.perform(delete("/metric/{id}", id)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 验证删除后查询应返回 null 或空
        mockMvc.perform(get("/metric/{id}", id))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void testGetFields() throws Exception {
        // 传入一个真实存在的表名，例如 "metric_definition"
        String tableName = "metric_definition";
        mockMvc.perform(get("/metric/fields")
                        .param("tableName", tableName)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }
}