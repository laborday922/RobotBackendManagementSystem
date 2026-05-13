package com.ruoyi.function.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.function.domain.SysMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = com.ruoyi.function.TestApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("地图管理Controller测试")
public class SysMapControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMultipartFile testImageFile;

    @BeforeEach
    void setUp() {
        byte[] imageBytes = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==".getBytes();
        testImageFile = new MockMultipartFile(
                "file",
                "test-map.png",
                "image/png",
                imageBytes
        );
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("获取地图列表 - 成功")
    void testGetMapList() throws Exception {
        mockMvc.perform(get("/func/map/list")
                        .param("pageNum", "1")
                        .param("pageSize", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("新增地图 - 成功")
    void testAddMap() throws Exception {
        SysMap map = new SysMap();
        map.setMapName("新增测试地图");
        map.setRobotId("robot_001");
        map.setStatus("1");

        mockMvc.perform(post("/func/map")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(map)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("修改地图 - 成功")
    void testUpdateMap() throws Exception {
        SysMap map = new SysMap();
        map.setMapId(1L);
        map.setMapName("修改后的地图名称");
        map.setRobotId("robot_001");

        mockMvc.perform(put("/func/map")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(map)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("删除地图 - 成功")
    void testDeleteMap() throws Exception {
        mockMvc.perform(delete("/func/map/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}