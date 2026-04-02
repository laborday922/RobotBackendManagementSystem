package com.ruoyi.mode.service.impl;

import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.mode.domain.SysRobot;
import com.ruoyi.mode.mapper.SysRobotMapper;
import com.ruoyi.mode.mapper.SysModeHistoryMapper;
import com.ruoyi.mode.service.ISysRobotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 机器人Service业务层处理
 *
 * @author ruoyi
 */
@EnableAsync
@Service
public class SysRobotServiceImpl implements ISysRobotService
{
    private static final Logger logger = LoggerFactory.getLogger(SysRobotServiceImpl.class);

    @Autowired
    private SysRobotMapper sysRobotMapper;

    @Autowired
    private SysModeHistoryMapper sysModeHistoryMapper;

    // ==================== 基础CRUD方法 ====================

    @Override
    public SysRobot selectSysRobotById(Long robotId)
    {
        logger.debug("查询机器人: robotId={}", robotId);
        return sysRobotMapper.selectSysRobotById(robotId);
    }

    @Override
    public List<SysRobot> selectSysRobotList(SysRobot sysRobot)
    {
        logger.info("========== 查询机器人列表 ==========");
        logger.info("查询条件: robotName={}, status={}, currentMode={}, groupId={}",
                sysRobot != null ? sysRobot.getRobotName() : null,
                sysRobot != null ? sysRobot.getStatus() : null,
                sysRobot != null ? sysRobot.getCurrentMode() : null,
                sysRobot != null ? sysRobot.getGroupId() : null);

        List<SysRobot> list = sysRobotMapper.selectSysRobotList(sysRobot);

        logger.info("查询结果数量: {}", list.size());
        for (SysRobot robot : list) {
            logger.info("机器人: id={}, name={}, status={}, taskStatus={}, battery={}, delFlag={}",
                    robot.getRobotId(), robot.getRobotName(), robot.getStatus(),
                    robot.getTaskStatus(), robot.getBattery(), robot.getDelFlag());
        }
        logger.info("==================================");

        return list;
    }

    @Override
    public int insertSysRobot(SysRobot sysRobot)
    {
        logger.info("新增机器人: robotName={}, robotCode={}", sysRobot.getRobotName(), sysRobot.getRobotCode());
        sysRobot.setCreateTime(DateUtils.getNowDate());
        return sysRobotMapper.insertSysRobot(sysRobot);
    }

    @Override
    public int updateSysRobot(SysRobot sysRobot)
    {
        logger.info("更新机器人: robotId={}, robotName={}", sysRobot.getRobotId(), sysRobot.getRobotName());
        sysRobot.setUpdateTime(DateUtils.getNowDate());
        return sysRobotMapper.updateSysRobot(sysRobot);
    }

    @Override
    public int deleteSysRobotByIds(Long[] robotIds)
    {
        logger.info("批量删除机器人: robotIds={}", robotIds);
        return sysRobotMapper.deleteSysRobotByIds(robotIds);
    }

    @Override
    public int deleteSysRobotById(Long robotId)
    {
        logger.info("删除机器人: robotId={}", robotId);
        return sysRobotMapper.deleteSysRobotById(robotId);
    }

    @Override
    @Transactional
    public int updateRobotMode(Long robotId, Long modeId)
    {
        logger.info("更新机器人模式: robotId={}, modeId={}", robotId, modeId);
        SysRobot robot = sysRobotMapper.selectSysRobotById(robotId);
        if (robot == null) {
            logger.warn("机器人不存在: robotId={}", robotId);
            return 0;
        }

        int result = sysRobotMapper.updateRobotMode(robotId, modeId);

        if (result > 0) {
            logger.info("机器人模式切换成功: robotId={}, oldMode={}, newMode={}",
                    robotId, robot.getCurrentMode(), modeId);
        }

        return result;
    }

    // ==================== 快捷操作方法 ====================

    @Override
    public int batchRestartAsync(Long[] robotIds)
    {
        logger.info("========== 异步批量重启机器人（立即返回） ==========");
        logger.info("机器人ID列表: {}", Arrays.toString(robotIds));

        int submittedCount = 0;
        String operator = SecurityUtils.getUsername();

        for (Long robotId : robotIds) {
            try {
                SysRobot robot = sysRobotMapper.selectSysRobotById(robotId);
                if (robot == null) {
                    logger.warn("机器人不存在: robotId={}", robotId);
                    continue;
                }

                logger.info("提交重启任务: robotId={}, robotName={}, currentStatus={}, battery={}",
                        robotId, robot.getRobotName(), robot.getStatus(), robot.getBattery());

                Long originalBattery = robot.getBattery();

                SysRobot updateRobot = new SysRobot();
                updateRobot.setRobotId(robotId);
                updateRobot.setStatus(2); // 待激活状态
                updateRobot.setTaskStatus(3); // 维护状态
                updateRobot.setBattery(originalBattery);
                updateRobot.setUpdateTime(DateUtils.getNowDate());

                int updateResult = sysRobotMapper.updateSysRobot(updateRobot);
                logger.info("设置重启中状态结果: robotId={}, updateResult={}", robotId, updateResult);

                recordOperation(robotId, robot.getRobotName(), "batch_restart_start",
                        "success", operator, "开始重启");

                asyncRestartRobot(robotId, robot.getRobotName(), operator, originalBattery);

                submittedCount++;
                logger.info("机器人重启任务已提交: robotId={}", robotId);

            } catch (Exception e) {
                logger.error("提交机器人重启任务失败: robotId={}", robotId, e);
            }
        }

        logger.info("异步批量重启完成: 已提交={}", submittedCount);
        logger.info("========== 异步批量重启结束 ==========");

        return submittedCount;
    }

    @Override
    @Transactional
    public int batchRestart(Long[] robotIds)
    {
        logger.info("========== 同步批量重启机器人 ==========");
        logger.info("机器人ID列表: {}", Arrays.toString(robotIds));

        int successCount = 0;
        String operator = SecurityUtils.getUsername();

        for (Long robotId : robotIds) {
            try {
                SysRobot robot = sysRobotMapper.selectSysRobotById(robotId);
                if (robot == null) {
                    logger.warn("机器人不存在: robotId={}", robotId);
                    continue;
                }

                Long originalBattery = robot.getBattery();
                logger.info("处理机器人: robotId={}, robotName={}", robotId, robot.getRobotName());

                robot.setStatus(2);
                robot.setTaskStatus(3);
                robot.setUpdateTime(DateUtils.getNowDate());
                sysRobotMapper.updateSysRobot(robot);

                recordOperation(robotId, robot.getRobotName(), "batch_restart_start",
                        "success", operator, "开始重启");

                syncRestartRobot(robotId, robot.getRobotName(), operator, originalBattery);

                successCount++;
                logger.info("机器人重启完成: robotId={}", robotId);

            } catch (Exception e) {
                logger.error("机器人重启失败: robotId={}", robotId, e);
            }
        }

        logger.info("同步批量重启完成: 成功={}", successCount);
        return successCount;
    }

    private void syncRestartRobot(Long robotId, String robotName, String operator, Long originalBattery) {
        logger.info("开始同步重启机器人: robotId={}", robotId);

        try {
            Thread.sleep(1000);

            SysRobot robot = sysRobotMapper.selectSysRobotById(robotId);
            if (robot != null) {
                robot.setStatus(1);
                robot.setTaskStatus(2);
                robot.setBattery(originalBattery);
                if (robot.getCurrentMode() == null) {
                    robot.setCurrentMode(1L);
                }
                robot.setUpdateTime(DateUtils.getNowDate());
                sysRobotMapper.updateSysRobot(robot);

                logger.info("同步重启成功: robotId={}, robotName={}", robotId, robotName);
                recordOperation(robotId, robotName, "batch_restart_complete",
                        "success", operator, "重启完成");
            }
        } catch (InterruptedException e) {
            logger.error("同步重启被中断: robotId={}", robotId, e);
            Thread.currentThread().interrupt();
            updateRobotToOffline(robotId, robotName);
        } catch (Exception e) {
            logger.error("同步重启失败: robotId={}", robotId, e);
            updateRobotToOffline(robotId, robotName);
        }
    }

    @Async
    public void asyncRestartRobot(Long robotId, String robotName, String operator, Long originalBattery) {
        logger.info("========== 异步重启线程开始 ==========");
        logger.info("线程ID: {}, 机器人ID: {}", Thread.currentThread().getId(), robotId);

        try {
            Thread.sleep(1000);

            boolean updateSuccess = updateRobotAfterRestartWithRetry(robotId, robotName, operator, originalBattery);

            if (!updateSuccess) {
                logger.error("重启后状态更新失败: robotId={}", robotId);
                forceUpdateRobotStatus(robotId, originalBattery);
            }

        } catch (InterruptedException e) {
            logger.error("机器人异步重启被中断: robotId={}", robotId, e);
            Thread.currentThread().interrupt();
            updateRobotToOffline(robotId, robotName);
        } catch (Exception e) {
            logger.error("机器人异步重启失败: robotId={}", robotId, e);
            updateRobotToOffline(robotId, robotName);
        }

        logger.info("========== 异步重启线程结束 ==========");
    }

    private boolean updateRobotAfterRestartWithRetry(Long robotId, String robotName, String operator, Long originalBattery) {
        int maxRetries = 3;
        int retryCount = 0;

        while (retryCount < maxRetries) {
            try {
                logger.info("尝试更新重启后状态: robotId={}, 第{}次尝试", robotId, retryCount + 1);

                SysRobot robot = sysRobotMapper.selectSysRobotById(robotId);
                if (robot == null) {
                    logger.error("机器人不存在: robotId={}", robotId);
                    return false;
                }

                robot.setStatus(1);
                robot.setTaskStatus(2);
                robot.setBattery(originalBattery);
                if (robot.getCurrentMode() == null) {
                    robot.setCurrentMode(1L);
                }
                robot.setUpdateTime(DateUtils.getNowDate());

                int updateResult = sysRobotMapper.updateSysRobot(robot);
                logger.info("执行更新结果: robotId={}, updateResult={}", robotId, updateResult);

                Thread.sleep(500);

                SysRobot afterRobot = sysRobotMapper.selectSysRobotById(robotId);
                if (afterRobot != null && afterRobot.getStatus() == 1) {
                    logger.info("✓ 机器人重启成功: robotId={}, robotName={}", robotId, robotName);
                    recordOperation(robotId, robotName, "batch_restart_complete",
                            "success", operator, "重启完成");
                    return true;
                } else {
                    logger.warn("状态验证失败: 期望状态1, 实际{}", afterRobot != null ? afterRobot.getStatus() : "null");
                    retryCount++;
                    Thread.sleep(1000);
                }
            } catch (Exception e) {
                logger.error("更新状态异常: robotId={}", robotId, e);
                retryCount++;
                try { Thread.sleep(1000); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
            }
        }
        return false;
    }

    private void forceUpdateRobotStatus(Long robotId, Long originalBattery) {
        logger.info("开始强制更新机器人状态: robotId={}", robotId);

        try {
            SysRobot robot = new SysRobot();
            robot.setRobotId(robotId);
            robot.setStatus(1);
            robot.setTaskStatus(2);
            robot.setBattery(originalBattery);
            robot.setCurrentMode(1L);
            robot.setUpdateTime(DateUtils.getNowDate());

            int result = sysRobotMapper.updateSysRobot(robot);
            logger.info("强制更新结果: robotId={}, result={}", robotId, result);
        } catch (Exception e) {
            logger.error("强制更新失败: robotId={}", robotId, e);
        }
    }

    private void updateRobotToOffline(Long robotId, String robotName) {
        try {
            SysRobot robot = sysRobotMapper.selectSysRobotById(robotId);
            if (robot != null) {
                robot.setStatus(0);
                robot.setTaskStatus(2);
                robot.setUpdateTime(DateUtils.getNowDate());
                sysRobotMapper.updateSysRobot(robot);
                logger.warn("机器人重启失败，设置为离线: robotId={}", robotId);
                recordOperation(robotId, robotName, "batch_restart_failed",
                        "fail", SecurityUtils.getUsername(), "重启失败");
            }
        } catch (Exception ex) {
            logger.error("更新状态失败: robotId={}", robotId, ex);
        }
    }

    @Override
    @Transactional
    public int emergencyStop(Long[] robotIds)
    {
        logger.info("执行紧急停止: robotIds={}", robotIds);
        int successCount = 0;
        String operator = SecurityUtils.getUsername();

        for (Long robotId : robotIds) {
            SysRobot robot = sysRobotMapper.selectSysRobotById(robotId);
            if (robot != null && robot.getStatus() != 0) {
                robot.setStatus(0);
                robot.setTaskStatus(2);
                robot.setUpdateTime(DateUtils.getNowDate());
                sysRobotMapper.updateSysRobot(robot);

                recordOperation(robotId, robot.getRobotName(), "emergency_stop", "success", operator, "紧急停止");
                successCount++;
                logger.info("紧急停止成功: robotId={}", robotId);
            }
        }
        return successCount;
    }

    @Override
    public int refreshStatus(Long[] robotIds)
    {
        logger.info("刷新机器人状态: robotIds={}", robotIds);
        int successCount = 0;
        String operator = SecurityUtils.getUsername();

        for (Long robotId : robotIds) {
            SysRobot robot = sysRobotMapper.selectSysRobotById(robotId);
            if (robot != null) {
                if (robot.getStatus() == 1 || robot.getStatus() == 0) {
                    int change = (int)(Math.random() * 10) - 3;
                    int newBattery = Math.max(0, Math.min(100, robot.getBattery().intValue() + change));
                    robot.setBattery((long)newBattery);

                    if (newBattery <= 15) {
                        robot.setTaskStatus(1);
                    } else {
                        robot.setTaskStatus(2);
                    }

                    robot.setUpdateTime(DateUtils.getNowDate());
                    sysRobotMapper.updateSysRobot(robot);
                }

                recordOperation(robotId, robot.getRobotName(), "refresh_status", "success", operator, "刷新状态");
                successCount++;
            }
        }
        return successCount;
    }

    @Override
    @Transactional
    public int testAlert(Long[] robotIds)
    {
        logger.info("测试告警: robotIds={}", robotIds);
        int successCount = 0;
        String operator = SecurityUtils.getUsername();

        for (int i = 0; i < robotIds.length && i < 2; i++) {
            Long robotId = robotIds[i];
            SysRobot robot = sysRobotMapper.selectSysRobotById(robotId);
            if (robot != null && robot.getStatus() != 0) {
                robot.setTaskStatus(1);
                robot.setBattery(15L);
                robot.setHardwareStatus(1);
                robot.setUpdateTime(DateUtils.getNowDate());
                sysRobotMapper.updateSysRobot(robot);

                recordOperation(robotId, robot.getRobotName(), "test_alert", "success", operator, "触发低电量测试告警");
                successCount++;
            }
        }
        return successCount;
    }

    @Override
    @Transactional
    public int clearAlerts(Long[] robotIds)
    {
        logger.info("清除告警: robotIds={}", robotIds);
        int successCount = 0;
        String operator = SecurityUtils.getUsername();

        for (Long robotId : robotIds) {
            SysRobot robot = sysRobotMapper.selectSysRobotById(robotId);
            if (robot != null) {
                robot.setHardwareStatus(0);
                if (robot.getBattery() < 15) {
                    robot.setBattery(30L);
                }
                if (robot.getTaskStatus() == 1) {
                    robot.setTaskStatus(2);
                }
                robot.setUpdateTime(DateUtils.getNowDate());
                sysRobotMapper.updateSysRobot(robot);

                recordOperation(robotId, robot.getRobotName(), "clear_alerts", "success", operator, "清除告警");
                successCount++;
            }
        }
        return successCount;
    }

    @Override
    public List<SysRobot> selectOnlineRobots()
    {
        logger.debug("查询在线机器人");
        SysRobot condition = new SysRobot();
        condition.setStatus(1);
        return sysRobotMapper.selectSysRobotList(condition);
    }

    @Override
    public List<SysRobot> selectLowBatteryRobots(Integer threshold)
    {
        logger.debug("查询低电量机器人: threshold={}", threshold);
        return sysRobotMapper.selectLowBatteryRobots(threshold);
    }

    // ==================== 模式切换操作 ====================

    @Override
    @Transactional
    public int standbyMode(Long[] robotIds)
    {
        logger.info("切换待机模式: robotIds={}", robotIds);
        int successCount = 0;
        String operator = SecurityUtils.getUsername();
        Long standbyModeId = getModeIdByName("待机模式");

        for (Long robotId : robotIds) {
            try {
                SysRobot robot = sysRobotMapper.selectSysRobotById(robotId);
                if (robot != null && robot.getStatus() != 0) {
                    robot.setCurrentMode(standbyModeId);
                    robot.setStatus(2);
                    robot.setTaskStatus(2);
                    robot.setUpdateTime(DateUtils.getNowDate());
                    sysRobotMapper.updateSysRobot(robot);

                    recordOperation(robotId, robot.getRobotName(), "standby_mode", "success", operator, "切换为待机模式");
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
        logger.info("切换维护模式: robotIds={}", robotIds);
        int successCount = 0;
        String operator = SecurityUtils.getUsername();
        Long maintenanceModeId = getModeIdByName("维护模式");

        for (Long robotId : robotIds) {
            try {
                SysRobot robot = sysRobotMapper.selectSysRobotById(robotId);
                if (robot != null && robot.getStatus() != 0) {
                    robot.setCurrentMode(maintenanceModeId);
                    robot.setTaskStatus(3);
                    robot.setUpdateTime(DateUtils.getNowDate());
                    sysRobotMapper.updateSysRobot(robot);

                    recordOperation(robotId, robot.getRobotName(), "maintenance_mode", "success", operator, "切换为维护模式");
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
        logger.info("切换充电模式: robotIds={}", robotIds);
        int successCount = 0;
        String operator = SecurityUtils.getUsername();
        Long chargeModeId = getModeIdByName("充电模式");

        for (Long robotId : robotIds) {
            try {
                SysRobot robot = sysRobotMapper.selectSysRobotById(robotId);
                if (robot != null && robot.getStatus() != 0) {
                    robot.setCurrentMode(chargeModeId);
                    robot.setTaskStatus(1);
                    robot.setUpdateTime(DateUtils.getNowDate());
                    sysRobotMapper.updateSysRobot(robot);

                    recordOperation(robotId, robot.getRobotName(), "charge_mode", "success", operator, "切换为充电模式");
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
        logger.info("返回充电: robotIds={}", robotIds);
        int successCount = 0;
        String operator = SecurityUtils.getUsername();
        Long chargeModeId = getModeIdByName("充电模式");

        for (Long robotId : robotIds) {
            try {
                SysRobot robot = sysRobotMapper.selectSysRobotById(robotId);
                if (robot != null && robot.getStatus() != 0) {
                    robot.setCurrentMode(chargeModeId);
                    robot.setTaskStatus(1);
                    robot.setUpdateTime(DateUtils.getNowDate());
                    sysRobotMapper.updateSysRobot(robot);

                    recordOperation(robotId, robot.getRobotName(), "return_charge", "success", operator, "返回充电");
                    successCount++;
                    logger.info("返回充电成功: robotId={}", robotId);
                }
            } catch (Exception e) {
                logger.error("返回充电失败: robotId={}", robotId, e);
            }
        }
        return successCount;
    }

    // ==================== 模式配置相关方法 ====================

    @Override
    @Transactional
    public int saveRobotModeConfig(Long robotId, Long modeId, Map<String, Object> config)
    {
        logger.info("保存机器人模式配置: robotId={}, modeId={}, config={}", robotId, modeId, config);
        return 1;
    }

    @Override
    public Map<String, Object> getRobotModeConfig(Long robotId, Long modeId)
    {
        logger.info("获取机器人模式配置: robotId={}, modeId={}", robotId, modeId);
        Map<String, Object> config = new HashMap<>();
        config.put("param_1", "default_value");
        config.put("param_2", 50);
        config.put("param_3", true);
        return config;
    }

    @Override
    @Transactional
    public int deleteRobotModeConfig(Long robotId, Long modeId)
    {
        logger.info("删除机器人模式配置: robotId={}, modeId={}", robotId, modeId);
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
            return saveRobotModeConfig(targetRobotId, modeId, config);
        }
        return 0;
    }

    // ==================== 私有辅助方法 ====================

    private void recordOperation(Long robotId, String robotName, String operationType,
                                 String result, String operator, String remark)
    {
        try {
            logger.info("操作记录: robotId={}, robotName={}, operationType={}, result={}, operator={}, remark={}",
                    robotId, robotName, operationType, result, operator, remark);
        } catch (Exception e) {
            logger.error("记录操作失败", e);
        }
    }

    private Long getModeIdByName(String modeName)
    {
        Map<String, Long> modeMap = new HashMap<>();
        modeMap.put("待机模式", 1L);
        modeMap.put("维护模式", 2L);
        modeMap.put("充电模式", 3L);
        modeMap.put("巡检模式", 4L);
        modeMap.put("作业模式", 5L);
        return modeMap.getOrDefault(modeName, 1L);
    }
}