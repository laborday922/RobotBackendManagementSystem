package com.ruoyi.taskmgt.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.taskmgt.TestApplication;
import com.ruoyi.taskmgt.controller.dto.TaskDto;
import com.ruoyi.taskmgt.controller.dto.TemplateDto;
import com.ruoyi.taskmgt.mock.WithMockRuoyiUser;
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

import java.util.ArrayList;
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
public class TaskLogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RedisCache redisCache;

    @Test
    void testListLogs_WithPagination() throws Exception {
        mockMvc.perform(get("/taskmgt/logs")
                        .param("pageNum", "1")
                        .param("pageSize", "10")
                        .param("eventType", "CREATE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.rows", notNullValue()));
    }

    @Test
    void testListLogs_WithTimeRange() throws Exception {
        mockMvc.perform(get("/taskmgt/logs")
                        .param("beginTime", "2024-01-01 00:00:00")
                        .param("endTime", "2024-12-31 23:59:59")
                        .param("operator", "admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void testGetLogInfo() throws Exception {
        // 假设存在 id=1 的日志
        mockMvc.perform(get("/taskmgt/logs/{id}", 1L))
                .andExpect(status().isOk());
    }

    @Test
    void testListByTask() throws Exception {
        mockMvc.perform(get("/taskmgt/logs/tasks")
                .param("taskId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void testListLogs_WithTaskIdAndStepId() throws Exception {
        mockMvc.perform(get("/taskmgt/logs")
                        .param("taskId", "1")
                        .param("stepId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    private Long createTestTask() throws Exception {
        TemplateDto templateDto = new TemplateDto();
        templateDto.setName("通用测试模板");
        templateDto.setAppId(7L);
        templateDto.setRules(new ArrayList<>());
        MvcResult templateResult = mockMvc.perform(post("/taskmgt/template")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(templateDto))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();
        AjaxResult templateAjax = objectMapper.readValue(templateResult.getResponse().getContentAsString(), AjaxResult.class);
        Long testTemplateId = Long.valueOf(((Map<?, ?>) templateAjax.get("data")).get("id").toString());
        TaskDto dto = new TaskDto();
        dto.setName("日志测试任务");
        dto.setTemplateId(testTemplateId);
        dto.setTaskType(1);
        MvcResult result = mockMvc.perform(post("/taskmgt/task")
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