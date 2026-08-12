package com.ruoyi.mode.config;

import com.ruoyi.mode.constants.ModeConstants;
import com.ruoyi.mode.domain.SysMode;
import com.ruoyi.mode.domain.SysModeParam;
import com.ruoyi.mode.service.ISysModeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

/**
 * 模式模块配置 - 初始化默认模式和参数
 */
@Configuration
public class ModeConfig implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(ModeConfig.class);

    @Autowired
    private ISysModeService sysModeService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        logger.info("初始化模式模块配置...");
        initDefaultModes();
        initDefaultParams();
        logger.info("模式模块配置初始化完成");
    }

    /**
     * 初始化默认模式（如果数据库为空）
     */
    private void initDefaultModes() {
        try {
            SysMode query = new SysMode();
            query.setDelFlag(ModeConstants.DEL_FLAG_NORMAL);
            List<SysMode> modes = sysModeService.selectSysModeList(query);

            if (modes == null || modes.isEmpty()) {
                logger.info("未检测到模式数据，开始初始化默认模式...");

                // 待机模式
                SysMode standby = new SysMode();
                standby.setModeName("待机模式");
                standby.setModeType(ModeConstants.MODE_TYPE_SYSTEM);
                standby.setModeColor("#1890FF");
                standby.setModeIcon("fa fa-pause-circle");
                standby.setDescription("机器人待机状态，低功耗运行");
                standby.setEnabled(ModeConstants.ENABLED);
                standby.setOrderNum(1);
                sysModeService.insertSysMode(standby);

                // 维修模式
                SysMode maintenance = new SysMode();
                maintenance.setModeName("维修模式");
                maintenance.setModeType(ModeConstants.MODE_TYPE_SYSTEM);
                maintenance.setModeColor("#FAAD14");
                maintenance.setModeIcon("fa fa-tools");
                maintenance.setDescription("机器人维护检修状态，暂停全部任务");
                maintenance.setEnabled(ModeConstants.ENABLED);
                maintenance.setOrderNum(2);
                sysModeService.insertSysMode(maintenance);

                // 充电模式
                SysMode charge = new SysMode();
                charge.setModeName("充电模式");
                charge.setModeType(ModeConstants.MODE_TYPE_SYSTEM);
                charge.setModeColor("#52C41A");
                charge.setModeIcon("fa fa-battery-full");
                charge.setDescription("机器人充电状态");
                charge.setEnabled(ModeConstants.ENABLED);
                charge.setOrderNum(3);
                sysModeService.insertSysMode(charge);

                logger.info("默认模式初始化完成");
            }
        } catch (Exception e) {
            logger.error("初始化默认模式失败", e);
        }
    }

    /**
     * 初始化各模式的默认参数（如果对应模式的参数为空）
     */
    private void initDefaultParams() {
        try {
            // 模式ID常量：1=待机，2=维修，3=充电
            long[] modeIds = {1L, 2L, 3L};

            for (long modeId : modeIds) {
                Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM sys_mode_param WHERE mode_id = ? AND del_flag = '0'", Integer.class, modeId);
                if (count != null && count > 0) {
                    continue; // 已有参数，跳过
                }
                logger.info("初始化模式 (modeId={}) 的默认参数...", modeId);
                insertDefaultParams(modeId);
            }
        } catch (Exception e) {
            logger.error("初始化默认参数失败", e);
        }
    }

    private void insertDefaultParams(long modeId) {
        String sql = "INSERT INTO sys_mode_param (mode_id, param_name, param_label, param_type, param_description, " +
                "param_value, param_options, param_min, param_max, param_unit, order_num, del_flag, " +
                "tenant_id, create_by, create_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, '0', 0, 'system', NOW())";

        Object[][] params;
        switch ((int) modeId) {
            case 1: // 待机模式
                params = new Object[][]{
                    {1L, "stationary",      "保持静止",   "boolean", "机器人保持静止不动",                   "false", null, 0, 1, "", 1},
                    {1L, "disable_sensors", "关闭传感器", "boolean", "关闭部分非必要传感器以省电",            "false", null, 0, 1, "", 2}
                };
                break;
            case 2: // 维修模式
                params = new Object[][]{
                    {2L, "pause_all_tasks",   "暂停全部任务", "boolean", "暂停当前所有正在执行的任务",      "true",  null,                                                                                                                                              0, 1, "", 1},
                    {2L, "maintenance_level", "维护权限",     "select",  "维护操作所需的权限级别",          "basic", "[{\"label\":\"基础维护\",\"value\":\"basic\"},{\"label\":\"高级维护\",\"value\":\"advanced\"}]", 0, 1, "", 2},
                    {2L, "warning_enabled",   "警告提醒",     "boolean", "维护期间是否启用警告提示",           "true",  null,                                                                                                                                              0, 1, "", 3}
                };
                break;
            case 3: // 充电模式
                params = new Object[][]{
                    {3L, "charge_strategy",  "充电策略",     "select",  "选择充电执行策略",            "after_task", "[{\"label\":\"完成任务后充电\",\"value\":\"after_task\"},{\"label\":\"立即充电\",\"value\":\"immediate\"}]", 0,   2,  "", 1},
                    {3L, "charge_threshold", "充电电量阈值", "range",  "低电量触发充电的电量阈值",     "20",         null,                                                                                                     0, 100, "%", 2},
                    {3L, "charge_reminder",  "充电完成提醒", "boolean", "充电完成后是否发送通知提醒",    "true",       null,                                                                                                     0,   1,  "", 3}
                };
                break;
            default:
                return;
        }

        for (Object[] row : params) {
            jdbcTemplate.update(sql, row);
        }
        logger.info("模式 (modeId={}) 默认参数初始化完成", modeId);
    }
}
