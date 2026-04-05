package com.ruoyi.mode.utils;

import com.ruoyi.mode.domain.*;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;

/**
 * 测试数据构建器
 */
public class TestDataBuilder {

    // 构建测试用模式
    public static SysMode buildTestMode(Long modeId) {
        SysMode mode = new SysMode();
        mode.setModeId(modeId);
        mode.setModeName("测试模式" + modeId);
        mode.setModeType("custom");
        mode.setCategoryId(1L);
        mode.setModeColor("#1890FF");
        mode.setModeIcon("fa fa-test");
        mode.setDescription("这是测试用的模式");
        mode.setEnabled("1");
        mode.setUsageCount(0L);
        mode.setRobotCount(0L);
        mode.setOrderNum(1);
        mode.setDelFlag("0");
        mode.setCreateTime(new Date());
        return mode;
    }

    // 构建测试用模式列表
    public static List<SysMode> buildTestModeList(int count) {
        List<SysMode> modes = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            modes.add(buildTestMode((long) i));
        }
        return modes;
    }

    // 构建测试用机器人
    public static SysRobot buildTestRobot(Long robotId) {
        SysRobot robot = new SysRobot();
        robot.setRobotId(robotId);
        robot.setRobotCode("ROBOT_" + robotId);
        robot.setRobotName("测试机器人" + robotId);
        robot.setGroupId(1L);
        robot.setManufacturer("测试厂家");
        robot.setArea("测试区域");
        robot.setStatus(1);
        robot.setHardwareStatus(0);
        robot.setTaskStatus(2);
        robot.setBattery(80L);
        robot.setCurrentMode(1L);
        robot.setDelFlag("0");
        robot.setCreateTime(new Date());
        return robot;
    }

    // 构建测试用机器人列表
    public static List<SysRobot> buildTestRobotList(int count) {
        List<SysRobot> robots = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            robots.add(buildTestRobot((long) i));
        }
        return robots;
    }

    // 构建测试用排程
    public static SysModeSchedule buildTestSchedule(Long scheduleId) {
        SysModeSchedule schedule = new SysModeSchedule();
        schedule.setScheduleId(scheduleId);
        schedule.setScheduleName("测试排程" + scheduleId);
        schedule.setModeId(1L);
        schedule.setModeName("待机模式");
        schedule.setExecuteTime("2026-04-05 10:00:00");
        schedule.setRepeatType("daily");
        schedule.setStartDate(new Date());
        schedule.setStartTime("10:00:00");
        schedule.setDuration(java.math.BigDecimal.valueOf(1));
        schedule.setStatus("running");
        schedule.setDelFlag("0");
        schedule.setCreateTime(new Date());
        return schedule;
    }

    // 构建测试用历史记录
    public static SysModeHistory buildTestHistory(Long historyId) {
        SysModeHistory history = new SysModeHistory();
        history.setHistoryId(historyId);
        history.setOperationTime(new Date());
        history.setOperationType("mode-switch");
        history.setRobotId(1L);
        history.setRobotName("测试机器人1");
        history.setModeId(1L);
        history.setModeName("待机模式");
        history.setContent("模式切换测试");
        history.setOperator("admin");
        history.setStatus("success");
        history.setCreateTime(new Date());
        return history;
    }
}