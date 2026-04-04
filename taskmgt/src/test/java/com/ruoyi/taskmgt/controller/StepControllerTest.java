package com.ruoyi.taskmgt.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.taskmgt.TestApplication;
import com.ruoyi.taskmgt.controller.dto.TaskDto;
import com.ruoyi.taskmgt.controller.dto.TaskStepDto;
import com.ruoyi.taskmgt.controller.dto.TemplateDto;
import com.ruoyi.taskmgt.domain.bo.Task;
import com.ruoyi.taskmgt.domain.bo.TaskStep;
import com.ruoyi.taskmgt.mock.WithMockRuoyiUser;
import com.ruoyi.taskmgt.service.IStepService;
import com.ruoyi.taskmgt.service.ITaskService;
import com.ruoyi.taskmgt.service.vo.TaskVo;
import org.junit.jupiter.api.BeforeEach;
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
import java.util.*;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = TestApplication.class)
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@WithMockRuoyiUser(authorities = {"*:*:*"})
public class StepControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RedisCache redisCache;

    private Long testTaskId;

    @Autowired
    private ITaskService taskService;

    @Autowired
    private IStepService stepService;

    @BeforeEach
    void setUp() throws Exception {
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
        // 创建一个父任务
        TaskDto taskDto = new TaskDto();
        taskDto.setName("步骤测试父任务");
        taskDto.setTemplateId(testTemplateId);
        taskDto.setTaskType(1);
        MvcResult result = mockMvc.perform(post("/taskmgt/task")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskDto))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();
        String json = result.getResponse().getContentAsString();
        AjaxResult ajaxResult = objectMapper.readValue(json, AjaxResult.class);
        Map<?, ?> data = (Map<?, ?>) ajaxResult.get("data");
        testTaskId = Long.valueOf(data.get("id").toString());
    }

    @Test
    void testCreateTaskSteps_Success() throws Exception {
        List<TaskStepDto> steps = Arrays.asList(
                createStepDto("步骤1", 1),
                createStepDto("步骤2", 2),
                createStepDto("步骤3", 3)
        );
        mockMvc.perform(post("/taskmgt/tasks/{id}/steps", testTaskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(steps))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data", hasSize(3)));
    }

    @Test
    void testCreateTaskSteps_EmptyList() throws Exception {
        List<TaskStepDto> emptySteps = Arrays.asList();
        mockMvc.perform(post("/taskmgt/tasks/{id}/steps", testTaskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptySteps))
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    void testGetTaskSteps() throws Exception {
        // 先创建几个步骤
        List<TaskStepDto> steps = Arrays.asList(createStepDto("步骤A", 1), createStepDto("步骤B", 2));
        mockMvc.perform(post("/taskmgt/tasks/{id}/steps", testTaskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(steps))
                        .with(csrf()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/taskmgt/tasks/{id}/steps", testTaskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data", notNullValue()));
    }

    @Test
    void testUpdateTaskSteps() throws Exception {
         Long testTaskId = this.taskService.retrieveTasks(Task.DISABLED,null,null,null,null,null,null,null).get(0).getId();

        List<TaskStepDto> updatedSteps = Arrays.asList(createStepDto("新步骤1", 1), createStepDto("新步骤2", 2));
        mockMvc.perform(put("/taskmgt/tasks/{id}/steps", testTaskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedSteps))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void testCompleteStep() throws Exception {
        TaskVo task = this.taskService.retrieveTasks(Task.EXECUTING,null,null,null,null,null,null,null).get(0);
        Long stepId = this.stepService.retrieveSteps(task.getId()).stream().filter(step-> Objects.equals(step.getStatus(), TaskStep.EXECUTING)).toList().get(0).getId();
        mockMvc.perform(put("/taskmgt/steps/{id}/complete", stepId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    private TaskStepDto createStepDto(String name, int order) {
        TaskStepDto dto = new TaskStepDto();
        dto.setStepName(name);
        dto.setOrderNum(order);
        return dto;
    }
}