package com.ruoyi.mode.mapper;

import com.ruoyi.mode.TestApplication;
import com.ruoyi.mode.domain.SysRobot;
import com.ruoyi.mode.utils.TestDataBuilder;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = TestApplication.class)
@ActiveProfiles("test")
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SysRobotMapperTest {

    @Autowired
    private SysRobotMapper sysRobotMapper;

    private SysRobot testRobot;

    @BeforeEach
    void setUp() {
        testRobot = TestDataBuilder.buildTestRobot(null);
        String timestamp = String.valueOf(System.currentTimeMillis());
        testRobot.setRobotCode("TEST_ROBOT_" + timestamp);
        testRobot.setRobotName("测试机器人_" + timestamp);
    }

    @Test
    @DisplayName("测试插入机器人")
    void testInsertSysRobot() {
        int result = sysRobotMapper.insertSysRobot(testRobot);
        assertThat(result).isEqualTo(1);
        assertThat(testRobot.getRobotId()).isNotNull();
    }

    @Test
    @DisplayName("测试查询机器人列表")
    void testSelectSysRobotList() {
        sysRobotMapper.insertSysRobot(testRobot);

        SysRobot query = new SysRobot();
        query.setStatus(1);

        List<SysRobot> robots = sysRobotMapper.selectSysRobotList(query);
        assertThat(robots).isNotEmpty();
    }

    @Test
    @DisplayName("测试查询在线机器人")
    void testSelectOnlineRobots() {
        sysRobotMapper.insertSysRobot(testRobot);

        List<SysRobot> onlineRobots = sysRobotMapper.selectOnlineRobots();
        assertThat(onlineRobots).isNotEmpty();
    }

    @Test
    @DisplayName("测试查询低电量机器人")
    void testSelectLowBatteryRobots() {
        SysRobot lowBatteryRobot = TestDataBuilder.buildTestRobot(null);
        lowBatteryRobot.setRobotCode("LOW_BATTERY_" + System.currentTimeMillis());
        lowBatteryRobot.setRobotName("低电量机器人");
        lowBatteryRobot.setBattery(15L);
        sysRobotMapper.insertSysRobot(lowBatteryRobot);

        List<SysRobot> lowBatteryRobots = sysRobotMapper.selectLowBatteryRobots(20);
        assertThat(lowBatteryRobots).isNotEmpty();
    }

    @Test
    @DisplayName("测试更新机器人模式")
    void testUpdateRobotMode() {
        sysRobotMapper.insertSysRobot(testRobot);

        int result = sysRobotMapper.updateRobotMode(testRobot.getRobotId(), 5L);
        assertThat(result).isEqualTo(1);

        SysRobot updated = sysRobotMapper.selectSysRobotById(testRobot.getRobotId());
        assertThat(updated.getCurrentMode()).isEqualTo(5L);
    }

    @Test
    @DisplayName("测试更新机器人电量")
    void testUpdateRobotBattery() {
        sysRobotMapper.insertSysRobot(testRobot);

        int result = sysRobotMapper.updateRobotBattery(testRobot.getRobotId(), 50L);
        assertThat(result).isEqualTo(1);

        SysRobot updated = sysRobotMapper.selectSysRobotById(testRobot.getRobotId());
        assertThat(updated.getBattery()).isEqualTo(50L);
    }
}