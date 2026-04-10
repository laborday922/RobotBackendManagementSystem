package com.ruoyi.mode.service.impl;

import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.mode.domain.SysRobot;
import com.ruoyi.mode.mapper.SysRobotMapper;
import com.ruoyi.mode.service.ISysModeService;
import com.ruoyi.mode.service.ISysRobotOperationService;
import com.ruoyi.mode.service.ISysRobotService;
import com.ruoyi.robots.controller.dto.RobotStatusDto;
import com.ruoyi.robots.domain.Robot;
import com.ruoyi.robots.mapper.RobotsMapper;
import com.ruoyi.robots.service.IRobotsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
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

    @Autowired
    private RobotsMapper robotsMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ISysModeService sysModeService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // 模式名称缓存
    private Map<Long, String> modeNameCache = new HashMap<>();

    // ==================== 辅助方法 ====================

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

    private SysRobot getOrCreateSysRobot(Long robotId) {
        SysRobot sysRobot = sysRobotMapper.selectSysRobotById(robotId);
        if (sysRobot == null) {
            sysRobot = new SysRobot();
            sysRobot.setRobotId(robotId);
            sysRobot.setCreateTime(DateUtils.getNowDate());
            sysRobot.setDelFlag("0");
            sysRobot.setNeedAutoCharge(0);
            sysRobotMapper.insertSysRobot(sysRobot);
            logger.info("创建机器人扩展信息: robotId={}", robotId);
        }
        return sysRobot;
    }

    /**
     * 根据模式ID获取模式名称
     */
    private String getModeNameById(Long modeId) {
        if (modeId == null) {
            return "未知";
        }
        if (modeNameCache.containsKey(modeId)) {
            return modeNameCache.get(modeId);
        }
        try {
            String sql = "SELECT mode_name FROM sys_mode WHERE mode_id = ? AND del_flag = '0'";
            String modeName = jdbcTemplate.queryForObject(sql, String.class, modeId);
            if (modeName != null) {
                modeNameCache.put(modeId, modeName);
                return modeName;
            }
        } catch (Exception e) {
            logger.warn("获取模式名称失败: modeId={}", modeId, e);
        }
        return "未知模式(" + modeId + ")";
    }

    /**
     * 获取机器人的充电策略配置
     * @param robotId 机器人ID
     * @param modeId 模式ID
     * @return "immediate" 立即充电, "after_task" 完成任务后充电, null 未配置
     */
    private String getChargeStrategy(Long robotId, Long modeId) {
        try {
            String configJson = sysRobotMapper.getRobotModeConfig(robotId, modeId);
            if (configJson != null && !configJson.isEmpty()) {
                // 使用 Jackson 解析 JSON
                Map<String, Object> configMap = objectMapper.readValue(configJson, Map.class);

                // 优先使用中文键名 "充电策略"（与数据库保存格式一致）
                Object strategy = configMap.get("充电策略");
                if (strategy == null) {
                    // 兼容英文键名
                    strategy = configMap.get("chargeStrategy");
                }

                if (strategy != null) {
                    String strategyStr = strategy.toString();
                    logger.debug("获取到充电策略: robotId={}, strategy={}", robotId, strategyStr);
                    if ("after_task".equals(strategyStr)) {
                        return "after_task";
                    } else if ("immediate".equals(strategyStr)) {
                        return "immediate";
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("获取充电策略失败: robotId={}, modeId={}", robotId, modeId, e);
        }
        return null;
    }

    @Override
    public Map<String, Object> getRobotModeConfig(Long robotId, Long modeId)
    {
        logger.info("获取机器人模式配置: robotId={}, modeId={}", robotId, modeId);
        try {
            String configJson = sysRobotMapper.getRobotModeConfig(robotId, modeId);
            if (configJson != null && !configJson.isEmpty()) {
                return objectMapper.readValue(configJson, Map.class);
            }
        } catch (Exception e) {
            logger.error("获取配置失败", e);
        }

        // 返回默认配置（使用中文键名）
        Map<String, Object> config = new HashMap<>();
        config.put("充电策略", "after_task");
        return config;
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
        if (sysRobot.getNeedAutoCharge() == null) {
            sysRobot.setNeedAutoCharge(0);
        }
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

        String oldModeName = getModeNameById(oldMode);
        String newModeName = getModeNameById(modeId);

        int result = sysRobotMapper.updateRobotMode(robotId, modeId);

        if (result > 0) {
            try {
                jdbcTemplate.update("UPDATE robots SET current_mode = ? WHERE id = ?", modeId, robotId);
                logger.info("同步更新 robots 表成功: robotId={}, modeId={}", robotId, modeId);
            } catch (Exception e) {
                logger.error("同步更新 robots 表失败: robotId={}, error={}", robotId, e.getMessage());
            }

            sysModeService.incrementUsageCount(modeId);
            sysModeService.updateRobotCountByModeId(modeId);
            if (oldMode != null) {
                sysModeService.updateRobotCountByModeId(oldMode);
            }
        }

        if (result > 0) {
            logger.info("机器人模式切换成功: robotId={}, oldMode={}, newMode={}",
                    robotId, oldMode, modeId);
            String remark = String.format("模式切换: %s -> %s", oldModeName, newModeName);
            recordOperation(robotId, null, "mode_switch", "success", null, remark);
        } else {
            logger.warn("机器人模式切换失败: robotId={}, modeId={}", robotId, modeId);
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
                Robot robot = robotsMapper.selectRobotsById(robotId);
                if (robot == null) {
                    logger.warn("机器人不存在: robotId={}", robotId);
                    continue;
                }

                String robotName = robot.getName();
                logger.info("提交重启任务: robotId={}, robotName={}", robotId, robotName);

                RobotStatusDto statusDto = convertToRobotStatusDto(robot);
                statusDto.setStatus(2);
                statusDto.setTaskStatus(3);
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

        logger.info("异步批量重启完成: 已提交={}, 总数={}", submittedCount, robotIds.length);
        return submittedCount;
    }

    @Async
    public void asyncRestartRobot(Long robotId, String robotName, String operator) {
        logger.info("异步重启线程开始: robotId={}, robotName={}", robotId, robotName);

        try {
            Thread.sleep(500);

            Robot robot = robotsMapper.selectRobotsById(robotId);
            if (robot != null) {
                RobotStatusDto statusDto = convertToRobotStatusDto(robot);
                statusDto.setStatus(1);
                statusDto.setTaskStatus(2);
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
            Robot robot = robotsMapper.selectRobotsById(robotId);
            if (robot != null) {
                RobotStatusDto statusDto = convertToRobotStatusDto(robot);
                statusDto.setStatus(0);
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
                Robot robot = robotsMapper.selectRobotsById(robotId);
                if (robot == null) {
                    logger.warn("机器人不存在: robotId={}", robotId);
                    continue;
                }

                String robotName = robot.getName();
                logger.info("处理机器人: robotId={}, robotName={}", robotId, robotName);

                RobotStatusDto statusDto = convertToRobotStatusDto(robot);
                statusDto.setStatus(2);
                statusDto.setTaskStatus(3);
                robotsService.updateRobotStatus(statusDto);

                recordOperation(robotId, robotName, "batch_restart_start",
                        "success", operator, "开始重启");

                Thread.sleep(2000);

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
                Robot robot = robotsMapper.selectRobotsById(robotId);
                if (robot != null && robot.getStatus() != 0) {
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
    @Transactional
    public int emergencyEvacuation(Long[] robotIds)
    {
        logger.info("执行紧急撤离: robotIds={}", Arrays.toString(robotIds));
        int successCount = 0;
        String operator = getCurrentUsername();

        Long safeModeId = getSafeEvacuationModeId();
        String safeModeName = getModeNameById(safeModeId);

        for (Long robotId : robotIds) {
            try {
                Robot robot = robotsMapper.selectRobotsById(robotId);
                if (robot != null && robot.getStatus() != 0) {
                    RobotStatusDto statusDto = convertToRobotStatusDto(robot);

                    statusDto.setTaskStatus(0);
                    statusDto.setStatus(2);
                    statusDto.setHardwareStatus(0);
                    robotsService.updateRobotStatus(statusDto);

                    updateRobotMode(robotId, safeModeId);

                    recordOperation(robotId, robot.getName(), "emergency_evacuation",
                            "success", operator, "紧急撤离 - 停止任务并返回安全位置(" + safeModeName + ")");

                    successCount++;
                    logger.info("紧急撤离成功: robotId={}, robotName={}", robotId, robot.getName());
                } else {
                    logger.warn("机器人不存在或不在线，无法执行紧急撤离: robotId={}", robotId);
                }
            } catch (Exception e) {
                logger.error("紧急撤离失败: robotId={}", robotId, e);
            }
        }

        logger.info("紧急撤离完成: 成功={}, 总数={}", successCount, robotIds.length);
        return successCount;
    }

    private Long getSafeEvacuationModeId() {
        try {
            String sql = "SELECT mode_id FROM sys_mode WHERE (mode_name LIKE '%安全%' OR mode_name LIKE '%撤离%') AND del_flag = '0' LIMIT 1";
            Long modeId = jdbcTemplate.queryForObject(sql, Long.class);
            if (modeId != null) {
                return modeId;
            }
        } catch (Exception e) {
            logger.warn("未找到安全撤离模式，使用待机模式作为默认");
        }
        return 1L;
    }

    @Override
    public int refreshStatus(Long[] robotIds)
    {
        logger.info("刷新机器人状态: robotIds={}", Arrays.toString(robotIds));
        int successCount = 0;
        String operator = getCurrentUsername();

        for (Long robotId : robotIds) {
            try {
                Robot robot = robotsMapper.selectRobotsById(robotId);
                if (robot != null) {
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

                    // 检查并执行自动充电
                    checkAndExecuteAutoCharge(robotId);

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

    /**
     * 检查并执行自动充电
     * 任务状态说明：
     * 0 - 执行中（有任务）
     * 1 - 充电中
     * 2 - 闲置（无任务）
     * 3 - 维护中
     */
    private void checkAndExecuteAutoCharge(Long robotId) {
        try {
            // 检查是否需要自动充电
            Integer needAutoCharge = sysRobotMapper.checkNeedAutoCharge(robotId);
            if (needAutoCharge != null && needAutoCharge == 1) {
                Robot robot = robotsMapper.selectRobotsById(robotId);
                // 检查任务是否已完成：task_status != 0 表示没有任务在执行
                if (robot != null && robot.getTaskStatus() != null && robot.getTaskStatus() != 0) {
                    logger.info("任务已完成（task_status={}），执行自动充电: robotId={}", robot.getTaskStatus(), robotId);
                    // 执行充电
                    chargeMode(new Long[]{robotId});
                    // 清除标记
                    sysRobotMapper.clearNeedAutoCharge(robotId);
                } else {
                    logger.debug("机器人仍有任务在执行（task_status=0），等待下次检查: robotId={}", robotId);
                }
            }
        } catch (Exception e) {
            logger.error("检查自动充电失败: robotId={}", robotId, e);
        }
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
                Robot robot = robotsMapper.selectRobotsById(robotId);
                if (robot != null && robot.getStatus() != 0) {
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
                Robot robot = robotsMapper.selectRobotsById(robotId);
                if (robot != null) {
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
        Robot condition = new Robot();
        condition.setStatus(1);
        List<Robot> allRobots = robotsService.selectRobotsList(condition);

        if (allRobots == null || allRobots.isEmpty()) {
            return new ArrayList<>();
        }

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
        String modeName = getModeNameById(standbyModeId);

        for (Long robotId : robotIds) {
            try {
                Robot robot = robotsMapper.selectRobotsById(robotId);
                if (robot != null && robot.getStatus() != 0) {
                    RobotStatusDto statusDto = convertToRobotStatusDto(robot);
                    statusDto.setStatus(2);
                    statusDto.setTaskStatus(2);
                    robotsService.updateRobotStatus(statusDto);

                    updateRobotMode(robotId, standbyModeId);

                    recordOperation(robotId, robot.getName(), "standby_mode",
                            "success", operator, "切换为" + modeName);
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
        String modeName = getModeNameById(maintenanceModeId);

        for (Long robotId : robotIds) {
            try {
                Robot robot = robotsMapper.selectRobotsById(robotId);
                if (robot != null && robot.getStatus() != 0) {
                    RobotStatusDto statusDto = convertToRobotStatusDto(robot);
                    statusDto.setTaskStatus(3);
                    robotsService.updateRobotStatus(statusDto);

                    updateRobotMode(robotId, maintenanceModeId);

                    recordOperation(robotId, robot.getName(), "maintenance_mode",
                            "success", operator, "切换为" + modeName);
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
    public Map<String, Object> chargeMode(Long[] robotIds)
    {
        logger.info("切换充电模式: robotIds={}", Arrays.toString(robotIds));
        int successCount = 0;
        int waitingCount = 0;
        int immediateCount = 0;
        String operator = getCurrentUsername();
        Long chargeModeId = 3L;
        String modeName = getModeNameById(chargeModeId);

        for (Long robotId : robotIds) {
            try {
                Robot robot = robotsMapper.selectRobotsById(robotId);
                if (robot != null && robot.getStatus() != 0) {
                    RobotStatusDto statusDto = convertToRobotStatusDto(robot);

                    // 获取充电策略
                    String chargeStrategy = getChargeStrategy(robotId, chargeModeId);
                    if (chargeStrategy == null) {
                        chargeStrategy = "after_task";
                        logger.info("机器人 {} 未配置充电策略，使用默认策略: 完成任务后充电", robot.getName());
                    }

                    logger.info("机器人 {} - 充电策略: {}, 当前任务状态: {}, 当前电量: {}%",
                            robot.getName(), chargeStrategy, statusDto.getTaskStatus(), statusDto.getBattery());

                    if ("immediate".equals(chargeStrategy)) {
                        // 立即充电策略
                        statusDto.setTaskStatus(1);
                        robotsService.updateRobotStatus(statusDto);
                        updateRobotMode(robotId, chargeModeId);
                        recordOperation(robotId, robot.getName(), "charge_mode",
                                "success", operator, "切换为" + modeName + "（立即充电）");
                        successCount++;
                        immediateCount++;
                        logger.info("立即充电成功: robotId={}, robotName={}", robotId, robot.getName());

                    } else if ("after_task".equals(chargeStrategy)) {
                        // 完成任务后充电策略
                        // taskStatus == 0 表示有任务在执行中
                        if (statusDto.getTaskStatus() != null && statusDto.getTaskStatus() == 0) {
                            // 有任务在执行中：标记自动充电，同时切换到充电模式
                            sysRobotMapper.markNeedAutoCharge(robotId);
                            // 立即切换模式到充电模式（但不改变任务状态）
                            updateRobotMode(robotId, chargeModeId);
                            recordOperation(robotId, robot.getName(), "charge_mode",
                                    "info", operator, "切换为" + modeName + "（任务完成后自动充电）");
                            logger.info("标记自动充电并切换模式: robotId={}, 当前任务状态=0（执行中），任务完成后自动充电", robotId);
                            successCount++;
                            waitingCount++;
                        } else {
                            // 无任务或任务已完成（taskStatus != 0）：立即充电
                            statusDto.setTaskStatus(1);
                            robotsService.updateRobotStatus(statusDto);
                            updateRobotMode(robotId, chargeModeId);
                            recordOperation(robotId, robot.getName(), "charge_mode",
                                    "success", operator, "切换为" + modeName + "（当前无任务，立即充电）");
                            logger.info("无任务（task_status={}），立即充电: robotId={}", statusDto.getTaskStatus(), robotId);
                            successCount++;
                            immediateCount++;
                        }
                    }
                } else {
                    logger.warn("机器人不存在或不在线，无法切换充电模式: robotId={}", robotId);
                }
            } catch (Exception e) {
                logger.error("切换充电模式失败: robotId={}", robotId, e);
            }
        }

        logger.info("充电模式切换完成: 成功={}, 立即充电={}, 等待充电={}, 总数={}",
                successCount, immediateCount, waitingCount, robotIds.length);

        Map<String, Object> result = new HashMap<>();
        result.put("successCount", successCount);
        result.put("immediateCount", immediateCount);
        result.put("waitingCount", waitingCount);
        result.put("total", robotIds.length);
        result.put("message", String.format("成功切换 %d 个机器人，其中 %d 个立即充电，%d 个等待任务完成后充电",
                successCount, immediateCount, waitingCount));

        return result;
    }

    @Override
    @Transactional
    public int returnCharge(Long[] robotIds)
    {
        logger.info("返回充电: robotIds={}", Arrays.toString(robotIds));
        int successCount = 0;
        String operator = getCurrentUsername();
        Long chargeModeId = 3L;
        String modeName = getModeNameById(chargeModeId);

        for (Long robotId : robotIds) {
            try {
                Robot robot = robotsMapper.selectRobotsById(robotId);
                if (robot != null && robot.getStatus() != 0) {
                    RobotStatusDto statusDto = convertToRobotStatusDto(robot);

                    String chargeStrategy = getChargeStrategy(robotId, chargeModeId);
                    if (chargeStrategy == null) {
                        chargeStrategy = "after_task";
                        logger.info("机器人 {} 未配置充电策略，使用默认策略: 完成任务后充电", robot.getName());
                    }

                    String remarkSuffix = chargeStrategy.equals("after_task") ? "（完成任务后充电）" : "（立即充电）";
                    logger.info("机器人 {} 充电策略: {}, 当前任务状态: {}", robot.getName(), chargeStrategy, statusDto.getTaskStatus());

                    if ("immediate".equals(chargeStrategy)) {
                        statusDto.setTaskStatus(1);
                        robotsService.updateRobotStatus(statusDto);
                        updateRobotMode(robotId, chargeModeId);
                        recordOperation(robotId, robot.getName(), "return_charge",
                                "success", operator, "返回充电 - " + modeName + remarkSuffix);
                        successCount++;
                        logger.info("返回充电成功: robotId={}", robotId);

                    } else if ("after_task".equals(chargeStrategy)) {
                        if (statusDto.getTaskStatus() != null && statusDto.getTaskStatus() == 0) {
                            sysRobotMapper.markNeedAutoCharge(robotId);
                            recordOperation(robotId, robot.getName(), "return_charge",
                                    "info", operator, "等待当前任务完成后自动返回充电" + remarkSuffix);
                            logger.info("标记自动返回充电: robotId={}, 当前任务状态=0（执行中）", robotId);
                            successCount++;
                        } else {
                            statusDto.setTaskStatus(1);
                            robotsService.updateRobotStatus(statusDto);
                            updateRobotMode(robotId, chargeModeId);
                            recordOperation(robotId, robot.getName(), "return_charge",
                                    "success", operator, "返回充电 - " + modeName + "（当前无任务，立即充电）");
                            logger.info("无任务（task_status={}），立即返回充电: robotId={}", statusDto.getTaskStatus(), robotId);
                            successCount++;
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("返回充电失败: robotId={}", robotId, e);
            }
        }

        logger.info("返回充电完成: 成功={}, 总数={}", successCount, robotIds.length);
        return successCount;
    }

    // ==================== 机器人模式配置相关方法 ====================

    @Override
    @Transactional
    public int saveRobotModeConfig(Long robotId, Long modeId, Map<String, Object> config)
    {
        logger.info("保存机器人模式配置: robotId={}, modeId={}, config={}", robotId, modeId, config);

        try {
            String configJson = objectMapper.writeValueAsString(config);
            int result = sysRobotMapper.saveRobotModeConfig(robotId, modeId, configJson);
            recordOperation(robotId, null, "save_config", "success",
                    getCurrentUsername(), "保存模式配置: modeId=" + modeId);
            return result;
        } catch (Exception e) {
            logger.error("保存配置失败", e);
            return 0;
        }
    }


    @Override
    @Transactional
    public int deleteRobotModeConfig(Long robotId, Long modeId)
    {
        logger.info("删除机器人模式配置: robotId={}, modeId={}", robotId, modeId);
        int result = sysRobotMapper.deleteRobotModeConfig(robotId, modeId);
        recordOperation(robotId, null, "delete_config", "success",
                getCurrentUsername(), "删除模式配置: modeId=" + modeId);
        return result;
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
                Robot robot = robotsMapper.selectRobotsById(robotId);
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