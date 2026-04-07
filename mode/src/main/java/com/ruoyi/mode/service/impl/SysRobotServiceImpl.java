package com.ruoyi.mode.service.impl;

import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.mode.domain.SysRobot;
import com.ruoyi.mode.mapper.SysRobotMapper;
import com.ruoyi.mode.service.ISysRobotOperationService;
import com.ruoyi.mode.service.ISysRobotService;
import com.ruoyi.robots.controller.dto.RobotStatusDto;
import com.ruoyi.robots.domain.Robot;
import com.ruoyi.robots.service.IRobotsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@EnableAsync
@Service
public class SysRobotServiceImpl implements ISysRobotService
{
    private static final Logger logger = LoggerFactory.getLogger(SysRobotServiceImpl.class);

    @Autowired
    private SysRobotMapper sysRobotMapper;

    @Autowired
    private ISysRobotOperationService robotOperationService;

    @Autowired
    private IRobotsService robotsService;

    // ==================== 辅助方法 ====================

    /**
     * 将 Robot 转换为 RobotStatusDto（用于更新动态状态）
     */
    private RobotStatusDto convertToRobotStatusDto(Robot robot) {
        if (robot == null) {
            return null;
        }
        RobotStatusDto dto = new RobotStatusDto();
        dto.setId(robot.getId());
        dto.setCode(robot.getCode());
        dto.setStatus(robot.getStatus());
        dto.setTaskStatus(robot.getTaskStatus());
        dto.setBattery(robot.getBattery());
        dto.setHardwareStatus(robot.getHardwareStatus());
        dto.setNote("状态更新");
        return dto;
    }

    /**
     * 获取或创建机器人扩展信息
     */
    private SysRobot getOrCreateSysRobot(Long robotId) {
        SysRobot sysRobot = sysRobotMapper.selectSysRobotById(robotId);
        if (sysRobot == null) {
            sysRobot = new SysRobot();
            sysRobot.setRobotId(robotId);
            sysRobot.setCreateTime(DateUtils.getNowDate());
            sysRobot.setDelFlag("0");
            sysRobotMapper.insertSysRobot(sysRobot);
        }
        return sysRobot;
    }

    // ==================== 基础CRUD方法 ====================

    @Override
    public SysRobot selectSysRobotById(Long robotId)
    {
        logger.debug("查询机器人扩展信息: robotId={}", robotId);
        return sysRobotMapper.selectSysRobotById(robotId);
    }

    @Override
    public List<SysRobot> selectSysRobotList(SysRobot sysRobot)
    {
        logger.info("查询机器人扩展信息列表");
        return sysRobotMapper.selectSysRobotList(sysRobot);
    }

    @Override
    public int insertSysRobot(SysRobot sysRobot)
    {
        logger.info("新增机器人扩展信息: robotId={}", sysRobot.getRobotId());
        sysRobot.setCreateTime(DateUtils.getNowDate());
        sysRobot.setDelFlag("0");
        return sysRobotMapper.insertSysRobot(sysRobot);
    }

    @Override
    public int updateSysRobot(SysRobot sysRobot)
    {
        logger.info("更新机器人扩展信息: robotId={}", sysRobot.getRobotId());
        sysRobot.setUpdateTime(DateUtils.getNowDate());
        return sysRobotMapper.updateSysRobot(sysRobot);
    }

    @Override
    public int deleteSysRobotByIds(Long[] robotIds)
    {
        logger.info("批量删除机器人扩展信息: robotIds={}", Arrays.toString(robotIds));
        return sysRobotMapper.deleteSysRobotByIds(robotIds);
    }

    @Override
    public int deleteSysRobotById(Long robotId)
    {
        logger.info("删除机器人扩展信息: robotId={}", robotId);
        return sysRobotMapper.deleteSysRobotById(robotId);
    }

    @Override
    @Transactional
    public int updateRobotMode(Long robotId, Long modeId)
    {
        logger.info("更新机器人模式: robotId={}, modeId={}", robotId, modeId);

        SysRobot sysRobot = getOrCreateSysRobot(robotId);
        Long oldMode = sysRobot.getCurrentMode();

        int result = sysRobotMapper.updateRobotMode(robotId, modeId);

        if (result > 0) {
            logger.info("机器人模式切换成功: robotId={}, oldMode={}, newMode={}",
                    robotId, oldMode, modeId);
            recordOperation(robotId, null, "mode_switch",
                    "success", null, "模式切换: " + oldMode + " -> " + modeId);
        }

        return result;
    }

    // ==================== 快捷操作方法 ====================

    @Override
    public int batchRestartAsync(Long[] robotIds)
    {
        logger.info("========== 异步批量重启机器人 ==========");
        logger.info("机器人ID列表: {}", Arrays.toString(robotIds));

        int submittedCount = 0;
        String operator = getCurrentUsername();

        for (Long robotId : robotIds) {
            try {
                Robot robot = robotsService.selectRobotsById(robotId);
                if (robot == null) {
                    logger.warn("机器人不存在: robotId={}", robotId);
                    continue;
                }

                String robotName = robot.getName();
                logger.info("提交重启任务: robotId={}, robotName={}", robotId, robotName);

                // 使用 RobotStatusDto 更新动态状态
                RobotStatusDto statusDto = convertToRobotStatusDto(robot);
                statusDto.setStatus(2);  // 待激活状态
                statusDto.setTaskStatus(3);  // 维护状态
                robotsService.updateRobotStatus(statusDto);

                getOrCreateSysRobot(robotId);

                recordOperation(robotId, robotName, "batch_restart_start",
                        "success", operator, "开始重启");

                asyncRestartRobot(robotId, robotName, operator);

                submittedCount++;
                logger.info("机器人重启任务已提交: robotId={}", robotId);

            } catch (Exception e) {
                logger.error("提交机器人重启任务失败: robotId={}", robotId, e);
            }
        }

        logger.info("异步批量重启完成: 已提交={}", submittedCount);
        return submittedCount;
    }

    @Async
    public void asyncRestartRobot(Long robotId, String robotName, String operator) {
        logger.info("异步重启线程开始: robotId={}", robotId);

        try {
            Thread.sleep(2000);

            Robot robot = robotsService.selectRobotsById(robotId);
            if (robot != null) {
                // 使用 RobotStatusDto 更新动态状态
                RobotStatusDto statusDto = convertToRobotStatusDto(robot);
                statusDto.setStatus(1);  // 在线状态
                statusDto.setTaskStatus(2);  // 闲置状态
                robotsService.updateRobotStatus(statusDto);

                logger.info("机器人重启成功: robotId={}, robotName={}", robotId, robotName);
                recordOperation(robotId, robotName, "batch_restart_complete",
                        "success", operator, "重启完成");
            }
        } catch (InterruptedException e) {
            logger.error("机器人异步重启被中断: robotId={}", robotId, e);
            Thread.currentThread().interrupt();
            updateRobotToOffline(robotId, robotName);
        } catch (Exception e) {
            logger.error("机器人异步重启失败: robotId={}", robotId, e);
            updateRobotToOffline(robotId, robotName);
        }

        logger.info("异步重启线程结束: robotId={}", robotId);
    }

    private void updateRobotToOffline(Long robotId, String robotName) {
        try {
            Robot robot = robotsService.selectRobotsById(robotId);
            if (robot != null) {
                // 使用 RobotStatusDto 更新动态状态
                RobotStatusDto statusDto = convertToRobotStatusDto(robot);
                statusDto.setStatus(0);  // 离线状态
                robotsService.updateRobotStatus(statusDto);
                logger.warn("机器人重启失败，设置为离线: robotId={}", robotId);
                recordOperation(robotId, robotName, "batch_restart_failed",
                        "fail", getCurrentUsername(), "重启失败");
            }
        } catch (Exception ex) {
            logger.error("更新机器人状态失败: robotId={}", robotId, ex);
        }
    }

    @Override
    public int batchRestart(Long[] robotIds)
    {
        logger.info("========== 同步批量重启机器人 ==========");
        logger.info("机器人ID列表: {}", Arrays.toString(robotIds));

        int successCount = 0;
        String operator = getCurrentUsername();

        for (Long robotId : robotIds) {
            try {
                Robot robot = robotsService.selectRobotsById(robotId);
                if (robot == null) {
                    logger.warn("机器人不存在: robotId={}", robotId);
                    continue;
                }

                String robotName = robot.getName();
                logger.info("处理机器人: robotId={}, robotName={}", robotId, robotName);

                // 使用 RobotStatusDto 更新动态状态
                RobotStatusDto statusDto = convertToRobotStatusDto(robot);
                statusDto.setStatus(2);
                statusDto.setTaskStatus(3);
                robotsService.updateRobotStatus(statusDto);

                recordOperation(robotId, robotName, "batch_restart_start",
                        "success", operator, "开始重启");

                Thread.sleep(1000);

                statusDto.setStatus(1);
                statusDto.setTaskStatus(2);
                robotsService.updateRobotStatus(statusDto);

                successCount++;
                logger.info("机器人重启完成: robotId={}", robotId);

                recordOperation(robotId, robotName, "batch_restart_complete",
                        "success", operator, "重启完成");

            } catch (InterruptedException e) {
                logger.error("机器人重启被中断: robotId={}", robotId, e);
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                logger.error("机器人重启失败: robotId={}", robotId, e);
            }
        }

        logger.info("同步批量重启完成: 成功={}", successCount);
        return successCount;
    }

    @Override
    @Transactional
    public int emergencyStop(Long[] robotIds)
    {
        logger.info("执行紧急停止: robotIds={}", Arrays.toString(robotIds));
        int successCount = 0;
        String operator = getCurrentUsername();

        for (Long robotId : robotIds) {
            try {
                Robot robot = robotsService.selectRobotsById(robotId);
                if (robot != null && robot.getStatus() != 0) {
                    // 使用 RobotStatusDto 更新动态状态
                    RobotStatusDto statusDto = convertToRobotStatusDto(robot);
                    statusDto.setStatus(0);
                    statusDto.setTaskStatus(2);
                    robotsService.updateRobotStatus(statusDto);

                    recordOperation(robotId, robot.getName(), "emergency_stop",
                            "success", operator, "紧急停止");
                    successCount++;
                    logger.info("紧急停止成功: robotId={}", robotId);
                }
            } catch (Exception e) {
                logger.error("紧急停止失败: robotId={}", robotId, e);
            }
        }
        return successCount;
    }

    @Override
    public int refreshStatus(Long[] robotIds)
    {
        logger.info("刷新机器人状态: robotIds={}", Arrays.toString(robotIds));
        int successCount = 0;
        String operator = getCurrentUsername();

        for (Long robotId : robotIds) {
            try {
                Robot robot = robotsService.selectRobotsById(robotId);
                if (robot != null) {
                    // 使用 RobotStatusDto 更新动态状态
                    RobotStatusDto statusDto = convertToRobotStatusDto(robot);

                    int change = (int)(Math.random() * 10) - 3;
                    int newBattery = Math.max(0, Math.min(100, robot.getBattery() + change));
                    statusDto.setBattery(newBattery);

                    if (newBattery <= 15) {
                        statusDto.setTaskStatus(1);
                    } else if (statusDto.getTaskStatus() == 1 && newBattery > 20) {
                        statusDto.setTaskStatus(2);
                    }

                    robotsService.updateRobotStatus(statusDto);

                    recordOperation(robotId, robot.getName(), "refresh_status",
                            "success", operator, "刷新状态, 当前电量:" + newBattery);
                    successCount++;
                    logger.info("刷新状态成功: robotId={}, battery={}", robotId, newBattery);
                }
            } catch (Exception e) {
                logger.error("刷新状态失败: robotId={}", robotId, e);
            }
        }
        return successCount;
    }

    @Override
    @Transactional
    public int testAlert(Long[] robotIds)
    {
        logger.info("测试告警: robotIds={}", Arrays.toString(robotIds));
        int successCount = 0;
        String operator = getCurrentUsername();

        for (int i = 0; i < robotIds.length && i < 2; i++) {
            Long robotId = robotIds[i];
            try {
                Robot robot = robotsService.selectRobotsById(robotId);
                if (robot != null && robot.getStatus() != 0) {
                    // 使用 RobotStatusDto 更新动态状态
                    RobotStatusDto statusDto = convertToRobotStatusDto(robot);
                    statusDto.setTaskStatus(1);
                    statusDto.setBattery(15);
                    statusDto.setHardwareStatus(1);
                    robotsService.updateRobotStatus(statusDto);

                    recordOperation(robotId, robot.getName(), "test_alert",
                            "success", operator, "触发低电量测试告警");
                    successCount++;
                    logger.info("测试告警成功: robotId={}", robotId);
                }
            } catch (Exception e) {
                logger.error("测试告警失败: robotId={}", robotId, e);
            }
        }
        return successCount;
    }

    @Override
    @Transactional
    public int clearAlerts(Long[] robotIds)
    {
        logger.info("清除告警: robotIds={}", Arrays.toString(robotIds));
        int successCount = 0;
        String operator = getCurrentUsername();

        for (Long robotId : robotIds) {
            try {
                Robot robot = robotsService.selectRobotsById(robotId);
                if (robot != null) {
                    // 使用 RobotStatusDto 更新动态状态
                    RobotStatusDto statusDto = convertToRobotStatusDto(robot);
                    statusDto.setHardwareStatus(0);
                    if (statusDto.getBattery() < 15) {
                        statusDto.setBattery(30);
                    }
                    if (statusDto.getTaskStatus() == 1 && statusDto.getBattery() > 20) {
                        statusDto.setTaskStatus(2);
                    }
                    robotsService.updateRobotStatus(statusDto);

                    recordOperation(robotId, robot.getName(), "clear_alerts",
                            "success", operator, "清除告警");
                    successCount++;
                    logger.info("清除告警成功: robotId={}", robotId);
                }
            } catch (Exception e) {
                logger.error("清除告警失败: robotId={}", robotId, e);
            }
        }
        return successCount;
    }

    @Override
    public List<SysRobot> selectOnlineRobots()
    {
        logger.debug("查询在线机器人扩展信息");
        Robot condition = new Robot();
        condition.setStatus(1);
        List<Robot> onlineRobots = robotsService.selectRobotsList(condition);

        if (onlineRobots == null || onlineRobots.isEmpty()) {
            return new ArrayList<>();
        }

        List<SysRobot> result = new ArrayList<>();
        for (Robot robot : onlineRobots) {
            SysRobot ext = sysRobotMapper.selectSysRobotById(robot.getId());
            if (ext != null) {
                result.add(ext);
            }
        }

        return result;
    }

    @Override
    public List<SysRobot> selectLowBatteryRobots(Integer threshold)
    {
        logger.debug("查询低电量机器人扩展信息: threshold={}", threshold);
        // 从robot模块获取低电量机器人列表
        Robot condition = new Robot();
        condition.setStatus(1);
        List<Robot> allRobots = robotsService.selectRobotsList(condition);

        if (allRobots == null || allRobots.isEmpty()) {
            return new ArrayList<>();
        }

        // 过滤低电量机器人
        List<Robot> lowBatteryRobots = new ArrayList<>();
        for (Robot robot : allRobots) {
            if (robot.getBattery() != null && robot.getBattery() <= threshold) {
                lowBatteryRobots.add(robot);
            }
        }

        List<SysRobot> result = new ArrayList<>();
        for (Robot robot : lowBatteryRobots) {
            SysRobot ext = sysRobotMapper.selectSysRobotById(robot.getId());
            if (ext != null) {
                result.add(ext);
            } else {
                ext = new SysRobot();
                ext.setRobotId(robot.getId());
                result.add(ext);
            }
        }

        return result;
    }

    // ==================== 模式切换操作接口 ====================

    @Override
    @Transactional
    public int standbyMode(Long[] robotIds)
    {
        logger.info("切换待机模式: robotIds={}", Arrays.toString(robotIds));
        int successCount = 0;
        String operator = getCurrentUsername();
        Long standbyModeId = 1L;

        for (Long robotId : robotIds) {
            try {
                Robot robot = robotsService.selectRobotsById(robotId);
                if (robot != null && robot.getStatus() != 0) {
                    // 使用 RobotStatusDto 更新动态状态
                    RobotStatusDto statusDto = convertToRobotStatusDto(robot);
                    statusDto.setStatus(2);
                    statusDto.setTaskStatus(2);
                    robotsService.updateRobotStatus(statusDto);

                    updateRobotMode(robotId, standbyModeId);

                    recordOperation(robotId, robot.getName(), "standby_mode",
                            "success", operator, "切换为待机模式");
                    successCount++;
                    logger.info("切换待机模式成功: robotId={}", robotId);
                }
            } catch (Exception e) {
                logger.error("切换待机模式失败: robotId={}", robotId, e);
            }
        }
        return successCount;
    }

    @Override
    @Transactional
    public int maintenanceMode(Long[] robotIds)
    {
        logger.info("切换维护模式: robotIds={}", Arrays.toString(robotIds));
        int successCount = 0;
        String operator = getCurrentUsername();
        Long maintenanceModeId = 2L;

        for (Long robotId : robotIds) {
            try {
                Robot robot = robotsService.selectRobotsById(robotId);
                if (robot != null && robot.getStatus() != 0) {
                    // 使用 RobotStatusDto 更新动态状态
                    RobotStatusDto statusDto = convertToRobotStatusDto(robot);
                    statusDto.setTaskStatus(3);
                    robotsService.updateRobotStatus(statusDto);

                    updateRobotMode(robotId, maintenanceModeId);

                    recordOperation(robotId, robot.getName(), "maintenance_mode",
                            "success", operator, "切换为维护模式");
                    successCount++;
                    logger.info("切换维护模式成功: robotId={}", robotId);
                }
            } catch (Exception e) {
                logger.error("切换维护模式失败: robotId={}", robotId, e);
            }
        }
        return successCount;
    }

    @Override
    @Transactional
    public int chargeMode(Long[] robotIds)
    {
        logger.info("切换充电模式: robotIds={}", Arrays.toString(robotIds));
        int successCount = 0;
        String operator = getCurrentUsername();
        Long chargeModeId = 3L;

        for (Long robotId : robotIds) {
            try {
                Robot robot = robotsService.selectRobotsById(robotId);
                if (robot != null && robot.getStatus() != 0) {
                    // 使用 RobotStatusDto 更新动态状态
                    RobotStatusDto statusDto = convertToRobotStatusDto(robot);
                    statusDto.setTaskStatus(1);
                    robotsService.updateRobotStatus(statusDto);

                    updateRobotMode(robotId, chargeModeId);

                    recordOperation(robotId, robot.getName(), "charge_mode",
                            "success", operator, "切换为充电模式");
                    successCount++;
                    logger.info("切换充电模式成功: robotId={}", robotId);
                }
            } catch (Exception e) {
                logger.error("切换充电模式失败: robotId={}", robotId, e);
            }
        }
        return successCount;
    }

    @Override
    @Transactional
    public int returnCharge(Long[] robotIds)
    {
        logger.info("返回充电: robotIds={}", Arrays.toString(robotIds));
        int successCount = 0;
        String operator = getCurrentUsername();
        Long chargeModeId = 3L;

        for (Long robotId : robotIds) {
            try {
                Robot robot = robotsService.selectRobotsById(robotId);
                if (robot != null && robot.getStatus() != 0) {
                    // 使用 RobotStatusDto 更新动态状态
                    RobotStatusDto statusDto = convertToRobotStatusDto(robot);
                    statusDto.setTaskStatus(1);
                    robotsService.updateRobotStatus(statusDto);

                    updateRobotMode(robotId, chargeModeId);

                    recordOperation(robotId, robot.getName(), "return_charge",
                            "success", operator, "返回充电");
                    successCount++;
                    logger.info("返回充电成功: robotId={}", robotId);
                }
            } catch (Exception e) {
                logger.error("返回充电失败: robotId={}", robotId, e);
            }
        }
        return successCount;
    }

    // ==================== 机器人模式配置相关方法 ====================

    @Override
    @Transactional
    public int saveRobotModeConfig(Long robotId, Long modeId, Map<String, Object> config)
    {
        logger.info("保存机器人模式配置: robotId={}, modeId={}, config={}", robotId, modeId, config);
        recordOperation(robotId, null, "save_config", "success",
                getCurrentUsername(), "保存模式配置: modeId=" + modeId);
        return 1;
    }

    @Override
    public Map<String, Object> getRobotModeConfig(Long robotId, Long modeId)
    {
        logger.info("获取机器人模式配置: robotId={}, modeId={}", robotId, modeId);
        Map<String, Object> config = new HashMap<>();
        config.put("speed", 50);
        config.put("power", 80);
        config.put("auto_mode", true);
        return config;
    }

    @Override
    @Transactional
    public int deleteRobotModeConfig(Long robotId, Long modeId)
    {
        logger.info("删除机器人模式配置: robotId={}, modeId={}", robotId, modeId);
        recordOperation(robotId, null, "delete_config", "success",
                getCurrentUsername(), "删除模式配置: modeId=" + modeId);
        return 1;
    }

    @Override
    @Transactional
    public int copyRobotModeConfig(Long sourceRobotId, Long targetRobotId, Long modeId)
    {
        logger.info("复制机器人模式配置: sourceRobotId={}, targetRobotId={}, modeId={}",
                sourceRobotId, targetRobotId, modeId);
        Map<String, Object> config = getRobotModeConfig(sourceRobotId, modeId);
        if (config != null && !config.isEmpty()) {
            int result = saveRobotModeConfig(targetRobotId, modeId, config);
            recordOperation(targetRobotId, null, "copy_config", "success",
                    getCurrentUsername(), "从机器人" + sourceRobotId + "复制模式配置");
            return result;
        }
        return 0;
    }

    // ==================== 私有辅助方法 ====================

    protected String getCurrentUsername() {
        try {
            String username = SecurityUtils.getUsername();
            if (username != null && !username.isEmpty()) {
                return username;
            }
        } catch (Exception e) {
            logger.warn("获取当前用户失败，使用默认用户", e);
        }
        return "system";
    }

    private void recordOperation(Long robotId, String robotName, String operationType,
                                 String result, String operator, String remark)
    {
        try {
            String currentUser = operator;
            if (currentUser == null || currentUser.isEmpty()) {
                currentUser = getCurrentUsername();
            }

            if (robotName == null || robotName.isEmpty()) {
                Robot robot = robotsService.selectRobotsById(robotId);
                if (robot != null) {
                    robotName = robot.getName();
                }
            }

            if (robotOperationService != null) {
                robotOperationService.recordOperation(robotId, robotName, operationType,
                        result, currentUser, remark);
                logger.debug("操作记录已保存: robotId={}, operationType={}", robotId, operationType);
            } else {
                logger.info("操作记录(未保存): robotId={}, robotName={}, operationType={}, result={}, operator={}, remark={}",
                        robotId, robotName, operationType, result, currentUser, remark);
            }
        } catch (Exception e) {
            logger.error("记录操作失败", e);
        }
    }
}