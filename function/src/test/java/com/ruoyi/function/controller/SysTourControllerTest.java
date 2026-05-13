//package com.ruoyi.function.controller;
//
//import com.ruoyi.function.BaseControllerTest;
//import com.ruoyi.function.TestApplication;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.ruoyi.function.domain.SysTourContent;
//import com.ruoyi.function.domain.SysTourGeneral;
//import com.ruoyi.function.domain.SysTourRoute;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.http.MediaType;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.Arrays;
//import java.util.List;
//
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@SpringBootTest(classes = TestApplication.class)
//@AutoConfigureMockMvc
//@ActiveProfiles("test")
//@Transactional
//@DisplayName("智能讲解Controller测试")
//class SysTourControllerTest extends BaseControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    // ========== 通用配置测试 ==========
//    @Test
//    @DisplayName("获取通用配置 - 成功")
//    void testGetGeneralConfig() throws Exception {
//        mockMvc.perform(get("/func/tour/general/1")
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.code").value(200));
//    }
//
//    @Test
//    @DisplayName("保存通用配置 - 成功")
//    void testSaveGeneralConfig() throws Exception {
//        SysTourGeneral config = new SysTourGeneral();
//        config.setRobotId(1L);
//        config.setMapId(1L);
//        config.setVoice("温柔女声");
//        config.setVoiceInteraction("1");
//        config.setStartCommand("开始讲解");
//        config.setBeforeTip("测试运动前提示");
//        config.setEndTip("测试讲解结束提示");
//        config.setAfterAction("stay");
//
//        mockMvc.perform(post("/func/tour/general")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(config)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.code").value(200));
//    }
//
//    // ========== 讲解内容测试 ==========
//    @Test
//    @DisplayName("获取讲解内容列表 - 成功")
//    void testGetContentList() throws Exception {
//        mockMvc.perform(get("/func/tour/content/list/1")
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.code").value(200))
//                .andExpect(jsonPath("$.data").isArray());
//    }
//
//    @Test
//    @DisplayName("获取讲解内容详情 - 成功")
//    void testGetContent() throws Exception {
//        mockMvc.perform(get("/func/tour/content/1")
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.code").value(200));
//    }
//
//    @Test
//    @DisplayName("保存讲解内容 - 成功")
//    void testSaveContent() throws Exception {
//        SysTourContent content = new SysTourContent();
//        content.setRobotId(1L);
//        content.setPointName("测试讲解点");
//        content.setPointDesc("测试描述");
//        content.setBroadcastType("text");
//        content.setBroadcastText("测试播报内容");
//        content.setVoiceType("温柔女声");
//        content.setSpeechRate(50);
//
//        mockMvc.perform(post("/func/tour/content")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(content)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.code").value(200));
//    }
//
//    @Test
//    @DisplayName("删除讲解内容 - 成功")
//    void testDeleteContent() throws Exception {
//        mockMvc.perform(delete("/func/tour/content/1")
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.code").value(200));
//    }
//
//    @Test
//    @DisplayName("批量删除讲解内容 - 成功")
//    void testBatchDeleteContents() throws Exception {
//        List<Long> contentIds = Arrays.asList(1L);
//        mockMvc.perform(delete("/func/tour/content/batch")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(contentIds)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.code").value(200));
//    }
//
//    // ========== 讲解路线测试 ==========
//    @Test
//    @DisplayName("获取路线列表 - 成功")
//    void testGetRouteList() throws Exception {
//        mockMvc.perform(get("/func/tour/route/list")
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.code").value(200))
//                .andExpect(jsonPath("$.data").isArray());
//    }
//
//    @Test
//    @DisplayName("获取路线详情 - 成功")
//    void testGetRoute() throws Exception {
//        mockMvc.perform(get("/func/tour/route/1")
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.code").value(200));
//    }
//
//    @Test
//    @DisplayName("保存路线 - 成功")
//    void testSaveRoute() throws Exception {
//        SysTourRoute route = new SysTourRoute();
//        route.setRouteName("测试路线");
//        route.setMapId(1L);
//        route.setStatus("1");
//
//        mockMvc.perform(post("/func/tour/route")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(route)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.code").value(200));
//    }
//
//    @Test
//    @DisplayName("删除路线 - 成功")
//    void testDeleteRoute() throws Exception {
//        mockMvc.perform(delete("/func/tour/route/1")
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.code").value(200));
//    }
//
//    @Test
//    @DisplayName("导出路线 - 成功")
//    void testExportRoutes() throws Exception {
//        mockMvc.perform(get("/func/tour/route/export")
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.code").value(200));
//    }
//
//    @Test
//    @DisplayName("导入路线 - 成功")
//    void testImportRoutes() throws Exception {
//        String jsonData = "[{\"routeName\":\"导入测试路线\",\"mapId\":1,\"status\":\"1\"}]";
//        mockMvc.perform(post("/func/tour/route/import")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(jsonData))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.code").value(200));
//    }
//}