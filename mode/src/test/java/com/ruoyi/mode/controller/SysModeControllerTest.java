package com.ruoyi.mode.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.mode.TestApplication;
import com.ruoyi.mode.config.TestSecurityConfig;
import com.ruoyi.mode.domain.SysMode;
import com.ruoyi.mode.service.ISysModeService;
import com.ruoyi.mode.utils.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = TestApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("模式Controller测试")
@Import(TestSecurityConfig.class)
@WithMockUser(username = "admin", authorities = {
        "system:mode:list",
        "system:mode:query",
        "system:mode:add",
        "system:mode:edit",
        "system:mode:remove"
})
class SysModeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ISysModeService sysModeService;

    private SysMode testMode;
    private List<SysMode> testModeList;

    @BeforeEach
    void setUp() {
        testMode = TestDataBuilder.buildTestMode(1L);
        testModeList = TestDataBuilder.buildTestModeList(5);
    }

    @Test
    @DisplayName("测试查询模式列表 - 成功")
    void testList_Success() throws Exception {
        when(sysModeService.selectSysModeList(any(SysMode.class))).thenReturn(testModeList);

        mockMvc.perform(get("/system/mode/list")
                        .param("pageNum", "1")
                        .param("pageSize", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("测试获取模式详情 - 成功")
    void testGetInfo_Success() throws Exception {
        when(sysModeService.selectSysModeById(1L)).thenReturn(testMode);

        mockMvc.perform(get("/system/mode/{modeId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.modeId").value(1))
                .andExpect(jsonPath("$.data.modeName").value("测试模式1"));
    }

    @Test
    @DisplayName("测试获取模式详情 - 不存在")
    void testGetInfo_NotFound() throws Exception {
        when(sysModeService.selectSysModeById(999L)).thenReturn(null);

        mockMvc.perform(get("/system/mode/{modeId}", 999L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("操作成功"));
        // 不检查 $.data，因为 null 值不会出现在 JSON 响应中
    }

    @Test
    @DisplayName("测试新增模式 - 成功")
    void testAdd_Success() throws Exception {
        when(sysModeService.insertSysMode(any(SysMode.class))).thenReturn(1);

        mockMvc.perform(post("/system/mode")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testMode)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(sysModeService).insertSysMode(any(SysMode.class));
    }

    @Test
    @DisplayName("测试修改模式 - 成功")
    void testEdit_Success() throws Exception {
        when(sysModeService.updateSysMode(any(SysMode.class))).thenReturn(1);

        mockMvc.perform(put("/system/mode")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testMode)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(sysModeService).updateSysMode(any(SysMode.class));
    }

    @Test
    @DisplayName("测试删除模式 - 成功")
    void testRemove_Success() throws Exception {
        when(sysModeService.deleteSysModeByIds(any())).thenReturn(2);

        mockMvc.perform(delete("/system/mode/{modeIds}", "1,2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(sysModeService).deleteSysModeByIds(any());
    }

    @Test
    @DisplayName("测试启用/禁用模式 - 成功")
    void testChangeStatus_Success() throws Exception {
        when(sysModeService.changeModeStatus(eq(1L), eq("0"))).thenReturn(1);

        SysMode statusMode = new SysMode();
        statusMode.setModeId(1L);
        statusMode.setEnabled("0");

        mockMvc.perform(put("/system/mode/changeStatus")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusMode)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(sysModeService).changeModeStatus(1L, "0");
    }
}