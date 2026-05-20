package com.ruoyi.mode.mapper;

import com.ruoyi.mode.ModeModuleTestApplication;
import com.ruoyi.mode.domain.SysRobot;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = ModeModuleTestApplication.class)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Transactional
public class SysRobotMapperTest {

    @Autowired
    private SysRobotMapper robotMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private ObjectMapper objectMapper = new ObjectMapper();

    private SysRobot testRobot;

    private static final Long TEST_ROBOT_ID = 999L;

    @BeforeEach
    void setUp() {
        // 清理可能存在的测试数据
        cleanupTestData();

        testRobot = new SysRobot();
        testRobot.setRobotId(TEST_ROBOT_ID);
        testRobot.setCurrentMode(1L);
        testRobot.setModeSwitchCount(0);
        testRobot.setNeedAutoCharge(0);
        testRobot.setTenantId(1L);
        testRobot.setDelFlag("0");
        testRobot.setCreateTime(new Date());
    }

    private void cleanupTestData() {
        try {
            // 删除可能存在的配置记录
            jdbcTemplate.update("DELETE FROM sys_robot_mode_config WHERE robot_id = ?", TEST_ROBOT_ID);
            // 删除机器人扩展记录
            jdbcTemplate.update("DELETE FROM sys_robot_ext WHERE robot_id = ?", TEST_ROBOT_ID);
        } catch (Exception e) {
            // 表可能不存在，忽略
        }
    }

    @Test
    @Order(1)
    void testInsertSysRobot() {
        int result = robotMapper.insertSysRobot(testRobot);
        assertThat(result).isEqualTo(1);
    }

    @Test
    @Order(2)
    void testSelectSysRobotById() {
        robotMapper.insertSysRobot(testRobot);

        SysRobot found = robotMapper.selectSysRobotById(TEST_ROBOT_ID);
        assertThat(found).isNotNull();
        assertThat(found.getCurrentMode()).isEqualTo(1L);
    }

    @Test
    @Order(3)
    void testSelectSysRobotList() {
        robotMapper.insertSysRobot(testRobot);

        SysRobot query = new SysRobot();
        query.setCurrentMode(1L);
        List<SysRobot> list = robotMapper.selectSysRobotList(query);

        assertThat(list).isNotEmpty();
    }

    @Test
    @Order(4)
    void testUpdateRobotMode() {
        robotMapper.insertSysRobot(testRobot);
        int result = robotMapper.updateRobotMode(TEST_ROBOT_ID, 2L);
        assertThat(result).isEqualTo(1);

        SysRobot updated = robotMapper.selectSysRobotById(TEST_ROBOT_ID);
        assertThat(updated.getCurrentMode()).isEqualTo(2L);
        assertThat(updated.getModeSwitchCount()).isEqualTo(1);
    }

    @Test
    @Order(5)
    void testBatchUpdateRobotMode() {
        robotMapper.insertSysRobot(testRobot);
        Long[] robotIds = {TEST_ROBOT_ID};
        int result = robotMapper.batchUpdateRobotMode(robotIds, 3L);
        assertThat(result).isEqualTo(1);
    }

    @Test
    @Order(6)
    void testMarkNeedAutoCharge() {
        robotMapper.insertSysRobot(testRobot);
        int result = robotMapper.markNeedAutoCharge(TEST_ROBOT_ID);
        assertThat(result).isEqualTo(1);

        int needAutoCharge = robotMapper.checkNeedAutoCharge(TEST_ROBOT_ID);
        assertThat(needAutoCharge).isEqualTo(1);
    }

    @Test
    @Order(7)
    void testClearNeedAutoCharge() {
        robotMapper.insertSysRobot(testRobot);
        robotMapper.markNeedAutoCharge(TEST_ROBOT_ID);
        int result = robotMapper.clearNeedAutoCharge(TEST_ROBOT_ID);
        assertThat(result).isEqualTo(1);

        int needAutoCharge = robotMapper.checkNeedAutoCharge(TEST_ROBOT_ID);
        assertThat(needAutoCharge).isEqualTo(0);
    }

    @Test
    @Order(8)
    void testSaveAndGetRobotModeConfig() throws Exception {
        robotMapper.insertSysRobot(testRobot);

        String config = "{\"test\":\"value\"}";
        int result = robotMapper.saveRobotModeConfig(TEST_ROBOT_ID, 1L, config);
        assertThat(result).isEqualTo(1);

        String retrieved = robotMapper.getRobotModeConfig(TEST_ROBOT_ID, 1L);

        // 使用 JSON 解析后比较，忽略空格差异
        Object expectedJson = objectMapper.readValue(config, Object.class);
        Object actualJson = objectMapper.readValue(retrieved, Object.class);
        assertThat(actualJson).isEqualTo(expectedJson);
    }

    @Test
    @Order(9)
    void testDeleteRobotModeConfig() {
        robotMapper.insertSysRobot(testRobot);
        robotMapper.saveRobotModeConfig(TEST_ROBOT_ID, 1L, "{\"test\":\"value\"}");
        int result = robotMapper.deleteRobotModeConfig(TEST_ROBOT_ID, 1L);
        assertThat(result).isEqualTo(1);
    }

    @Test
    @Order(10)
    void testSelectRobotModeStats() {
        robotMapper.insertSysRobot(testRobot);
        List<Map<String, Object>> stats = robotMapper.selectRobotModeStats();
        assertThat(stats).isNotNull();
    }

    @Test
    @Order(11)
    void testDeleteSysRobotById() {
        robotMapper.insertSysRobot(testRobot);
        int result = robotMapper.deleteSysRobotById(TEST_ROBOT_ID);
        assertThat(result).isEqualTo(1);
    }
}