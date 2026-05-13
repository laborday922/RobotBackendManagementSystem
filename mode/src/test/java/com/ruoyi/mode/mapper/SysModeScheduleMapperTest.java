package com.ruoyi.mode.mapper;

import com.ruoyi.mode.ModeModuleTestApplication;
import com.ruoyi.mode.domain.SysModeSchedule;
import com.ruoyi.mode.domain.SysRobot;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = ModeModuleTestApplication.class)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Transactional
public class SysModeScheduleMapperTest {

    @Autowired
    private SysModeScheduleMapper scheduleMapper;

    private SysModeSchedule testSchedule;

    @BeforeEach
    void setUp() {
        testSchedule = new SysModeSchedule();
        testSchedule.setScheduleName("测试排程");
        testSchedule.setModeId(1L);
        testSchedule.setModeName("待机模式");
        testSchedule.setExecuteTime("2026-04-15 10:00:00");
        testSchedule.setRepeatType("daily");
        testSchedule.setStartDate(new Date());
        testSchedule.setStartTime("10:00:00");
        testSchedule.setDuration(BigDecimal.valueOf(2));
        testSchedule.setStatus("pending");
        testSchedule.setDelFlag("0");
        testSchedule.setTenantId(1L);
    }

    @Test
    @Order(1)
    void testInsertSysModeSchedule() {
        int result = scheduleMapper.insertSysModeSchedule(testSchedule);
        assertThat(result).isEqualTo(1);
        assertThat(testSchedule.getScheduleId()).isNotNull();
    }

    @Test
    @Order(2)
    void testSelectSysModeScheduleById() {
        scheduleMapper.insertSysModeSchedule(testSchedule);
        Long scheduleId = testSchedule.getScheduleId();

        SysModeSchedule found = scheduleMapper.selectSysModeScheduleById(scheduleId);
        assertThat(found).isNotNull();
        assertThat(found.getScheduleName()).isEqualTo("测试排程");
    }

    @Test
    @Order(3)
    void testSelectSysModeScheduleList() {
        scheduleMapper.insertSysModeSchedule(testSchedule);

        SysModeSchedule query = new SysModeSchedule();
        query.setStatus("pending");
        List<SysModeSchedule> list = scheduleMapper.selectSysModeScheduleList(query);

        assertThat(list).isNotEmpty();
    }

    @Test
    @Order(4)
    void testUpdateSysModeSchedule() {
        scheduleMapper.insertSysModeSchedule(testSchedule);
        testSchedule.setScheduleName("更新后的排程名称");
        testSchedule.setStatus("running");

        int result = scheduleMapper.updateSysModeSchedule(testSchedule);
        assertThat(result).isEqualTo(1);

        SysModeSchedule updated = scheduleMapper.selectSysModeScheduleById(testSchedule.getScheduleId());
        assertThat(updated.getScheduleName()).isEqualTo("更新后的排程名称");
        assertThat(updated.getStatus()).isEqualTo("running");
    }

    @Test
    @Order(5)
    void testUpdateScheduleStatus() {
        scheduleMapper.insertSysModeSchedule(testSchedule);
        int result = scheduleMapper.updateScheduleStatus(testSchedule.getScheduleId(), "paused");
        assertThat(result).isEqualTo(1);

        SysModeSchedule updated = scheduleMapper.selectSysModeScheduleById(testSchedule.getScheduleId());
        assertThat(updated.getStatus()).isEqualTo("paused");
    }

    @Test
    @Order(6)
    void testInsertScheduleRobots() {
        scheduleMapper.insertSysModeSchedule(testSchedule);
        Long[] robotIds = {1L, 2L};
        int result = scheduleMapper.insertScheduleRobots(testSchedule.getScheduleId(), robotIds);
        assertThat(result).isGreaterThanOrEqualTo(0);
    }

    @Test
    @Order(7)
    void testSelectRobotsByScheduleId() {
        scheduleMapper.insertSysModeSchedule(testSchedule);
        Long[] robotIds = {1L};
        scheduleMapper.insertScheduleRobots(testSchedule.getScheduleId(), robotIds);

        List<SysRobot> robots = scheduleMapper.selectRobotsByScheduleId(testSchedule.getScheduleId(), 1L);
        assertThat(robots).isNotNull();
    }

    @Test
    @Order(8)
    void testDeleteScheduleRobots() {
        scheduleMapper.insertSysModeSchedule(testSchedule);
        int result = scheduleMapper.deleteScheduleRobots(testSchedule.getScheduleId());
        assertThat(result).isGreaterThanOrEqualTo(0);
    }

    @Test
    @Order(9)
    void testDeleteSysModeScheduleById() {
        scheduleMapper.insertSysModeSchedule(testSchedule);
        int result = scheduleMapper.deleteSysModeScheduleById(testSchedule.getScheduleId());
        assertThat(result).isEqualTo(1);
    }
}