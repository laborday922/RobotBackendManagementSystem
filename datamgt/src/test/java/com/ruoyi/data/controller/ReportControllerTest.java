package com.ruoyi.data.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.data.TestApplication;
import com.ruoyi.data.ai.controller.dto.ReportGenerateRequestDto;
import com.ruoyi.data.ai.service.AiAnalysisService;
import com.ruoyi.data.ai.service.TongYiService;
import com.ruoyi.data.mock.WithMockRuoyiUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = TestApplication.class)
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@WithMockRuoyiUser(username = "admin", userId = "1", authorities = {"*:*:*"}, deptId = "103")
public class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RedisCache redisCache;

    @MockBean
    private AiAnalysisService aiAnalysisService;

    @MockBean
    private TongYiService tongYiService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final Long TEST_REPORT_ID = 200L;

    @BeforeEach
    void setUp() {
        // 插入测试数据到 data_report 表（字段根据 Mapper XML 调整）
        jdbcTemplate.execute("INSERT INTO data_report (id, report_name, report_type, start_date, end_date, status, created_at, tenant_id) VALUES (" +
                TEST_REPORT_ID + ", '测试报告', 'daily', '2025-01-01', '2025-01-31', 1, NOW(), 1)");
        // 可选：插入关联的内容到 data_report_content 表（如果下载/详情需要）
        jdbcTemplate.execute("INSERT INTO data_report_content (report_id, content, tenant_id) VALUES (" +
                TEST_REPORT_ID + ", '这是测试报告内容', 1)");
    }

    @Test
    void testGenerate_Success() throws Exception {
        ReportGenerateRequestDto request = new ReportGenerateRequestDto();
        request.setReportType("weekly");
        request.setStartDate(String.valueOf(LocalDate.of(2025, 1, 1)));
        request.setEndDate(String.valueOf(LocalDate.of(2025, 1, 31)));
        request.setAnalysisDimension("sales");
        request.setCustomPrompt("重点关注");
        request.setReportDepth("detailed");

        mockMvc.perform(post("/ai/reports/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void testGenerate_MissingParams() throws Exception {
        ReportGenerateRequestDto request = new ReportGenerateRequestDto();
        request.setAnalysisDimension("sales");

        mockMvc.perform(post("/ai/reports/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.msg").value("参数不能为空"));
    }

    @Test
    void testList() throws Exception {
        mockMvc.perform(get("/ai/reports")
                        .param("page", "1")
                        .param("size", "10")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.rows").isArray())
                .andExpect(jsonPath("$.data.total").isNumber());
    }

    @Test
    void testGetDetail_Success() throws Exception {
        mockMvc.perform(get("/ai/reports/{id}", TEST_REPORT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(TEST_REPORT_ID))
                .andExpect(jsonPath("$.data.reportType").value("daily"));
    }

    @Test
    void testGetDetail_NotFound() throws Exception {
        mockMvc.perform(get("/ai/reports/{id}", 99999L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.msg").value("报告不存在"));
    }

    @Test
    void testDelete_Success() throws Exception {
        mockMvc.perform(delete("/ai/reports/{id}", TEST_REPORT_ID)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void testDelete_NotFound() throws Exception {
        mockMvc.perform(delete("/ai/reports/{id}", 99999L)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.msg").value("删除失败"));
    }

    @Test
    void testDownload() throws Exception {
        mockMvc.perform(get("/ai/reports/{id}/download", TEST_REPORT_ID)
                        .param("format", "pdf")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(header().exists("Content-Disposition"))
                .andExpect(header().string("Content-Type", "application/pdf"));
    }
}