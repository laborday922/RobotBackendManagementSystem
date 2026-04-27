package com.ruoyi.taskmgt.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.taskmgt.TestApplication;
import com.ruoyi.taskmgt.mock.WithMockRuoyiUser;
import com.ruoyi.taskmgt.monitor.AsyncOperationMonitor;
import com.ruoyi.taskmgt.monitor.dto.RobotCallbackData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = TestApplication.class)
@ActiveProfiles("test")
@WithMockRuoyiUser
@AutoConfigureMockMvc(addFilters = false)
public class RobotCallbackControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AsyncOperationMonitor asyncOperationMonitor;

    @Test
    void testReceiveRobotCallback_Success() throws Exception {
        RobotCallbackData callbackData = new RobotCallbackData();
        callbackData.setTraceId("trace-123");
        callbackData.setSuccess(true);
        callbackData.setData("操作完成");

        doNothing().when(asyncOperationMonitor).handleRobotCallback(eq("trace-123"), any(RobotCallbackData.class));

        mockMvc.perform(post("/taskmgt/callback/robot")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(callbackData)))
                .andExpect(status().isOk());

        verify(asyncOperationMonitor).handleRobotCallback("trace-123", callbackData);
    }

    @Test
    void testReceiveRobotCallback_FailureCallback() throws Exception {
        RobotCallbackData callbackData = new RobotCallbackData();
        callbackData.setTraceId("trace-456");
        callbackData.setSuccess(false);
        callbackData.setErrorMsg("执行失败");

        doNothing().when(asyncOperationMonitor).handleRobotCallback(eq("trace-456"), any(RobotCallbackData.class));

        mockMvc.perform(post("/taskmgt/callback/robot")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(callbackData)))
                .andExpect(status().isOk());

        verify(asyncOperationMonitor).handleRobotCallback("trace-456", callbackData);
    }

    @Test
    void testReceiveRobotCallback_EmptyTraceId() throws Exception {
        RobotCallbackData callbackData = new RobotCallbackData();
        callbackData.setTraceId("");
        callbackData.setSuccess(true);

        mockMvc.perform(post("/taskmgt/callback/robot")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(callbackData)))
                .andExpect(status().isOk());
    }
}