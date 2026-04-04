package com.ruoyi.taskmgt.controller;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.taskmgt.TestApplication;
import com.ruoyi.taskmgt.controller.dto.TemplateDto;
import com.ruoyi.taskmgt.mock.WithMockRuoyiUser;
import com.ruoyi.taskmgt.service.ITemplateService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = TestApplication.class)
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@WithMockRuoyiUser(authorities = {"*:*:*"})
public class TemplateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RedisCache redisCache;

    @Autowired
    private ITemplateService templateService;

    @Test
    void testCreateTemplate_Success() throws Exception {
        TemplateDto dto = createTemplateDto("测试模板");
        mockMvc.perform(post("/taskmgt/template")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id", notNullValue()));
    }

    @Test
    void testCreateTemplate_ValidationFailed() throws Exception {
        TemplateDto dto = new TemplateDto();  // 缺少必填字段
        mockMvc.perform(post("/taskmgt/template")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testRetrieveTemplates() throws Exception {
        createTestTemplate("模板A");
        createTestTemplate("模板B");
        mockMvc.perform(get("/taskmgt/templates")
                        .param("pageNum", "1")
                        .param("pageSize", "10")
                        .param("name", "模板"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.rows", notNullValue()));
    }

    @Test
    void testGetTemplate() throws Exception {
        Long templateId = createTestTemplateAndGetId("查询测试模板");
        mockMvc.perform(get("/taskmgt/templates/{id}", templateId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(templateId));
    }

    @Test
    void testUpdateTemplate() throws Exception {
        Long templateId = createTestTemplateAndGetId("更新测试模板");
        TemplateDto dto = createTemplateDto("已更新的模板名称");
        mockMvc.perform(put("/taskmgt/templates/{id}", templateId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void testDeleteTemplate() throws Exception {
        Long templateId = createTestTemplateAndGetId("待删除模板");
        mockMvc.perform(put("/taskmgt/templates/{id}/ban", templateId).with(csrf()));
        mockMvc.perform(delete("/taskmgt/templates/{id}", templateId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void testBanTemplate() throws Exception {
        Long templateId = createTestTemplateAndGetId("禁用测试模板");
        mockMvc.perform(put("/taskmgt/templates/{id}/ban", templateId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void testResumeTemplate() throws Exception {
        Long templateId = createTestTemplateAndGetId("恢复测试模板");
        // 先禁用
        mockMvc.perform(put("/taskmgt/templates/{id}/ban", templateId).with(csrf()));
        mockMvc.perform(put("/taskmgt/templates/{id}/resume", templateId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void testRetrieveTemplates_WithFilters() throws Exception {
        createTestTemplate("过滤测试模板");
        mockMvc.perform(get("/taskmgt/templates")
                        .param("appId", "1")
                        .param("status", "0")
                        .param("robotGroupId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    private TemplateDto createTemplateDto(String name) {
        TemplateDto dto = new TemplateDto();
        String uniqueName = name + "_" + System.currentTimeMillis();
        dto.setName(uniqueName);
        dto.setAppId(7L);
        dto.setDescription("测试描述");
        return dto;
    }

    private void createTestTemplate(String name) throws Exception {
        TemplateDto dto = createTemplateDto(name);
        mockMvc.perform(post("/taskmgt/template")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
                .with(csrf()));
    }

    private Long createTestTemplateAndGetId(String name) throws Exception {
        TemplateDto dto = createTemplateDto(name);
        MvcResult result = mockMvc.perform(post("/taskmgt/template")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();
        String json = result.getResponse().getContentAsString();
        AjaxResult ajaxResult = objectMapper.readValue(json, AjaxResult.class);
        Map<?, ?> data = (Map<?, ?>) ajaxResult.get("data");
        return Long.valueOf(data.get("id").toString());
    }
}