package com.ruoyi.taskmgt.controller;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.taskmgt.TestApplication;
import com.ruoyi.taskmgt.mock.WithMockRuoyiUser;
import com.ruoyi.taskmgt.service.ITaskParamsService;
import com.ruoyi.taskmgt.service.vo.DynamicParamVo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = TestApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockRuoyiUser
public class TaskParamsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ITaskParamsService taskParamsService;

    @Test
    void testGetDynamicParams_Success() throws Exception {
        Long apiId = 1L;
        Long robotId = 100L;
        DynamicParamVo vo = new DynamicParamVo();
        vo.setParamKey("testKey");
        vo.setParamName("测试参数");
        vo.setParamType("string");
        vo.setRequired(true);
        vo.setOptions(Collections.emptyList());

        when(taskParamsService.getDynamicParams(eq(apiId), eq(robotId)))
                .thenReturn(Collections.singletonList(vo));

        mockMvc.perform(get("/taskmgt/{id}/dynamic-params", apiId)
                        .param("robotId", String.valueOf(robotId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].paramKey").value("testKey"));
    }

    @Test
    void testGetDynamicParams_WithoutRobotId() throws Exception {
        Long apiId = 1L;
        when(taskParamsService.getDynamicParams(eq(apiId), isNull()))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/taskmgt/{id}/dynamic-params", apiId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }
}
