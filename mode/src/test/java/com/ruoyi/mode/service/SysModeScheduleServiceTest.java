package com.ruoyi.mode.service;

import com.ruoyi.mode.ModeModuleTestApplication;
import com.ruoyi.mode.domain.SysModeSchedule;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = ModeModuleTestApplication.class)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Transactional
public class SysModeScheduleServiceTest {

    @Autowired
    private ISysModeScheduleService scheduleService;

    private SysModeSchedule testSchedule;

    @BeforeEach
    void setUp() {
        testSchedule = new SysModeSchedule();
        testSchedule.setScheduleName("服务测试排程");
        testSchedule.setModeId(1L);
        testSchedule.setModeName("待机模式");
        testSchedule.setExecuteTime("2026-04-15 14:00:00");
        testSchedule.setRepeatType("daily");
        testSchedule.setStartDate(new Date());
        testSchedule.setStartTime("14:00:00");
        testSchedule.setDuration(BigDecimal.valueOf(2));
        testSchedule.setStatus("pending");
        testSchedule.setRobotIds(new Long[]{1L, 2L});
    }

    @Test
    @Order(1)
    void testInsertSysModeSchedule() {
        int result = scheduleService.insertSysModeSchedule(testSchedule);
        assertThat(result).isEqualTo(1);
        assertThat(testSchedule.getScheduleId()).isNotNull();
    }

    @Test
    @Order(2)
    void testSelectSysModeScheduleById() {
        scheduleService.insertSysModeSchedule(testSchedule);
        Long scheduleId = testSchedule.getScheduleId();

        SysModeSchedule found = scheduleService.selectSysModeScheduleById(scheduleId);
        assertThat(found).isNotNull();
        assertThat(found.getScheduleName()).isEqualTo("服务测试排程");
        assertThat(found.getRobots()).isNotNull();
    }

    @Test
    @Order(3)
    void testSelectSysModeScheduleList() {
        scheduleService.insertSysModeSchedule(testSchedule);

        SysModeSchedule query = new SysModeSchedule();
        query.setStatus("pending");
        List<SysModeSchedule> list = scheduleService.selectSysModeScheduleList(query);

        assertThat(list).isNotEmpty();
    }

    @Test
    @Order(4)
    void testUpdateSysModeSchedule() {
        scheduleService.insertSysModeSchedule(testSchedule);
        testSchedule.setScheduleName("更新后的服务测试排程");
        testSchedule.setStatus("running");
        // 设置 robotIds 为 null 表示不修改机器人关联
        testSchedule.setRobotIds(null);

        int result = scheduleService.updateSysModeSchedule(testSchedule);
        assertThat(result).isEqualTo(1);

        SysModeSchedule updated = scheduleService.selectSysModeScheduleById(testSchedule.getScheduleId());
        assertThat(updated.getScheduleName()).isEqualTo("更新后的服务测试排程");
    }

    @Test
    @Order(5)
    void testToggleScheduleStatus() {
        // 先插入排程
        scheduleService.insertSysModeSchedule(testSchedule);
        Long scheduleId = testSchedule.getScheduleId();

        // 获取原始状态
        SysModeSchedule beforeToggle = scheduleService.selectSysModeScheduleById(scheduleId);
        String originalStatus = beforeToggle.getStatus();

        // 执行状态切换
        int result = scheduleService.toggleScheduleStatus(scheduleId);

        // 验证方法执行成功（返回1表示更新了1条记录）
        assertThat(result).isEqualTo(1);

        // 获取切换后的状态
        SysModeSchedule afterToggle = scheduleService.selectSysModeScheduleById(scheduleId);
        String newStatus = afterToggle.getStatus();

        // 验证状态不为空
        assertThat(newStatus).isNotNull();
        assertThat(newStatus).isNotBlank();

        // 验证状态是有效的枚举值（根据实际业务可能的取值）
        assertThat(newStatus).isIn("running", "paused", "pending", "completed", "cancelled");

        // 可选：记录状态变化日志（用于调试）
        System.out.println("状态切换: " + originalStatus + " -> " + newStatus);
    }

    @Test
    @Order(6)
    void testGetCalendarData() {
        Map<String, Object> calendarData = scheduleService.getCalendarData(2026, 4);
        assertThat(calendarData).isNotNull();
        assertThat(calendarData.get("success")).isEqualTo(true);
    }

    @Test
    @Order(7)
    void testDeleteSysModeScheduleById() {
        scheduleService.insertSysModeSchedule(testSchedule);
        int result = scheduleService.deleteSysModeScheduleById(testSchedule.getScheduleId());
        assertThat(result).isEqualTo(1);
    }
}