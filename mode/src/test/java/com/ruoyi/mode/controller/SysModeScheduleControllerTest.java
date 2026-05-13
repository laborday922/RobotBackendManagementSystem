package com.ruoyi.mode.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.mode.ModeModuleTestApplication;
import com.ruoyi.mode.domain.SysModeSchedule;
import com.ruoyi.mode.service.ISysModeScheduleService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = ModeModuleTestApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Transactional
public class SysModeScheduleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ISysModeScheduleService scheduleService;

    private SysModeSchedule testSchedule;
    private Long testScheduleId;

    @BeforeEach
    void setUp() {
        testSchedule = new SysModeSchedule();
        testSchedule.setScheduleName("控制器测试排程");
        testSchedule.setModeId(1L);
        testSchedule.setModeName("待机模式");
        testSchedule.setExecuteTime("2026-04-15 16:00:00");
        testSchedule.setRepeatType("daily");
        testSchedule.setStartDate(new Date());
        testSchedule.setStartTime("16:00:00");
        testSchedule.setDuration(BigDecimal.valueOf(2));
        testSchedule.setStatus("pending");
        testSchedule.setRobotIds(new Long[]{1L});

        scheduleService.insertSysModeSchedule(testSchedule);
        testScheduleId = testSchedule.getScheduleId();
    }

    @Test
    @Order(1)
    void testListSchedule() throws Exception {
        mockMvc.perform(get("/system/schedule/list")
                        .param("pageNum", "1")
                        .param("pageSize", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @Order(2)
    void testGetInfo() throws Exception {
        mockMvc.perform(get("/system/schedule/{scheduleId}", testScheduleId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.scheduleName").value("控制器测试排程"));
    }

    @Test
    @Order(3)
    void testAddSchedule() throws Exception {
        SysModeSchedule newSchedule = new SysModeSchedule();
        newSchedule.setScheduleName("新增控制器排程");
        newSchedule.setModeId(1L);
        newSchedule.setModeName("待机模式");
        newSchedule.setExecuteTime("2026-04-16 10:00:00");
        newSchedule.setRepeatType("daily");
        newSchedule.setStartDate(new Date());
        newSchedule.setStartTime("10:00:00");
        newSchedule.setDuration(BigDecimal.valueOf(1));
        newSchedule.setStatus("pending");
        newSchedule.setRobotIds(new Long[]{1L, 2L});

        mockMvc.perform(post("/system/schedule")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newSchedule)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @Order(4)
    void testEditSchedule() throws Exception {
        testSchedule.setScheduleName("修改后的控制器排程");
        testSchedule.setDuration(BigDecimal.valueOf(3));

        mockMvc.perform(put("/system/schedule")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testSchedule)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @Order(5)
    void testToggleStatus() throws Exception {
        mockMvc.perform(put("/system/schedule/toggleStatus/{scheduleId}", testScheduleId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @Order(6)
    void testGetCalendarData() throws Exception {
        mockMvc.perform(get("/system/schedule/calendar")
                        .param("year", "2026")
                        .param("month", "4")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @Order(7)
    void testRemove() throws Exception {
        mockMvc.perform(delete("/system/schedule/{scheduleIds}", testScheduleId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}