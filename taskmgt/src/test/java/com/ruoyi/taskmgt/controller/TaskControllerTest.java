package com.ruoyi.taskmgt.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.taskmgt.TestApplication;
import com.ruoyi.taskmgt.controller.dto.TaskDto;
import com.ruoyi.taskmgt.controller.dto.TemplateDto;
import com.ruoyi.taskmgt.mock.WithMockRuoyiUser;
import com.ruoyi.taskmgt.service.ITaskService;
import com.ruoyi.taskmgt.domain.bo.Task;
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

import java.util.ArrayList;
import java.util.Map;

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
public class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RedisCache redisCache;

    private Long testTemplateId;

    @Autowired
    private ITaskService taskService;
    @BeforeEach
    void setUp() throws Exception {
        // 创建模板
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
        testTemplateId = Long.valueOf(((Map<?, ?>) templateAjax.get("data")).get("id").toString());
    }
    @Test
    void testCreateTask_Success() throws Exception {
        TaskDto dto = new TaskDto();
        dto.setName("测试任务_" + System.currentTimeMillis());
        dto.setTaskType(1);
        dto.setPriority(5);
        dto.setTemplateId(testTemplateId);
        mockMvc.perform(post("/taskmgt/task")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id", notNullValue()));
    }

    @Test
    void testCreateTask_ValidationFailed() throws Exception {
        TaskDto dto = new TaskDto();
        dto.setTaskType(1);  // 缺少 name
        dto.setTemplateId(testTemplateId);
        mockMvc.perform(post("/taskmgt/task")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testRetrieveTasks_WithPagination() throws Exception {
        // 先创建两个任务
        createTask("分页测试任务1");
        createTask("分页测试任务2");
        mockMvc.perform(get("/taskmgt/tasks")
                        .param("pageNum", "1")
                        .param("pageSize", "10")
                        .param("name", "分页测试"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.rows", hasSize(2)));
    }

    @Test
    void testUpdateTask_Success() throws Exception {
        Long taskId = createTaskAndGetId("更新测试任务");
        TaskDto dto = new TaskDto();
        dto.setName("已更新的任务名称");
        dto.setTaskType(2);
        mockMvc.perform(put("/taskmgt/tasks/{id}", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void testDeleteTask_Success() throws Exception {
        Long taskId = createTaskAndGetId("删除测试任务");
        mockMvc.perform(put("/taskmgt/tasks/{id}/ban", taskId).with(csrf()));
        mockMvc.perform(delete("/taskmgt/tasks/{id}", taskId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void testGetTask_Success() throws Exception {
        Long taskId = createTaskAndGetId("查询测试任务");
        mockMvc.perform(get("/taskmgt/tasks/{id}", taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(taskId));
    }

    @Test
    void testBanTask_Success() throws Exception {
        Long taskId = createTaskAndGetId("禁用测试任务");
        mockMvc.perform(put("/taskmgt/tasks/{id}/ban", taskId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void testResumeTask_Success() throws Exception {
        Long taskId = createTaskAndGetId("恢复测试任务");
        // 先禁用
        mockMvc.perform(put("/taskmgt/tasks/{id}/ban", taskId).with(csrf()));
        mockMvc.perform(put("/taskmgt/tasks/{id}/resume", taskId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void testPauseTask_Success() throws Exception {
        Long taskId = this.taskService.retrieveTasks(Task.EXECUTING,null,null,null,null,null,null,null).get(0).getId();
        mockMvc.perform(put("/taskmgt/tasks/{id}/pause", taskId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void testContinueTask_Success() throws Exception {
        Long taskId = this.taskService.retrieveTasks(Task.PAUSED,null,null,null,null,null,null,null).get(0).getId();
        mockMvc.perform(put("/taskmgt/tasks/{id}/continue", taskId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void testTerminateTask_WithReason() throws Exception {
        Long taskId = this.taskService.retrieveTasks(Task.EXECUTING,null,null,null,null,null,null,null).get(0).getId();
        TaskDto dto = new TaskDto();
        dto.setTerminateReason("测试终止原因");
        mockMvc.perform(put("/taskmgt/tasks/{id}/terminate", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void testCancelTask_Success() throws Exception {
        Long taskId = this.taskService.retrieveTasks(Task.PENDING,null,null,null,null,null,null,null).get(0).getId();
        mockMvc.perform(put("/taskmgt/tasks/{id}/cancel", taskId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void testListAbnormalTasks() throws Exception {
        mockMvc.perform(get("/taskmgt/tasks/abnormal")
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.rows", notNullValue()));
    }

    @Test
    void testGetAbnormalTask() throws Exception {
        // 注意：id=1 可能不存在，这里假设存在一个异常任务，或者先创建一个任务再制造异常
        mockMvc.perform(get("/taskmgt/tasks/1/abnormal"))
                .andExpect(status().isOk());
    }

    @Test
    void testResolveRisk() throws Exception {
        mockMvc.perform(put("/taskmgt/tasks/1/resolve")
                        .with(csrf()))
                .andExpect(status().isOk());
    }

//    @Test
//    void testUpdateGlobalOrder() throws Exception {
//        List<Long> taskIds = Arrays.asList(1L, 2L, 3L);
//        mockMvc.perform(put("/taskmgt/tasks/order/global")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(taskIds))
//                        .with(csrf()))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.code").value(200));
//    }
//
//    @Test
//    void testUpdateLocalOrder() throws Exception {
//        List<Long> taskIds = Arrays.asList(1L, 2L);
//        mockMvc.perform(put("/taskmgt/tasks/order/local")
//                        .param("robotId", "1")
//                        .param("isGroupId", "false")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(taskIds))
//                        .with(csrf()))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.code").value(200));
//    }

    // 辅助方法
    private void createTask(String name) throws Exception {
        TaskDto dto = new TaskDto();
        String uniqueName = name + "_" + System.currentTimeMillis();
        dto.setName(uniqueName);
        dto.setTemplateId(testTemplateId);
        dto.setTaskType(1);
        mockMvc.perform(post("/taskmgt/task")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
                .with(csrf()));
    }

    private Long createTaskAndGetId(String name) throws Exception {
        TaskDto dto = new TaskDto();
        String uniqueName = name + "_" + System.currentTimeMillis();
        dto.setName(uniqueName);
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