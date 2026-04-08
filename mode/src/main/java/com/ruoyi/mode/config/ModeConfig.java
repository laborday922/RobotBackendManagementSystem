package com.ruoyi.mode.config;

import com.ruoyi.common.threadlocal.TenantContext;
import com.ruoyi.mode.constants.ModeConstants;
import com.ruoyi.mode.domain.SysMode;
import com.ruoyi.mode.service.ISysModeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

/**
 * 模式模块配置
 */
@Configuration
public class ModeConfig implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(ModeConfig.class);

    @Autowired
    private ISysModeService sysModeService;

    /**
     * 获取当前租户ID
     */
    private Long getCurrentTenantId() {
        Long tenantId = TenantContext.get();
        return tenantId == null ? 0L : tenantId;
    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("初始化模式模块配置...");
        initDefaultModes();
        logger.info("模式模块配置初始化完成");
    }

    /**
     * 初始化默认模式（如果数据库为空）
     */
    private void initDefaultModes() {
        try {
            Long tenantId = getCurrentTenantId();

            // 系统租户（0）不自动初始化默认模式，由各租户自行初始化
            if (tenantId == 0) {
                logger.info("系统租户，跳过默认模式初始化");
                return;
            }

            SysMode query = new SysMode();
            query.setDelFlag(ModeConstants.DEL_FLAG_NORMAL);
            query.setTenantId(tenantId);

            java.util.List<SysMode> modes = sysModeService.selectSysModeList(query);

            if (modes == null || modes.isEmpty()) {
                logger.info("租户 {} 未检测到模式数据，开始初始化默认模式...", tenantId);

                // 待机模式
                SysMode standby = new SysMode();
                standby.setModeName("待机模式");
                standby.setModeType(ModeConstants.MODE_TYPE_SYSTEM);
                standby.setModeColor("#1890FF");
                standby.setModeIcon("fa fa-pause-circle");
                standby.setDescription("机器人待机状态，低功耗运行");
                standby.setEnabled(ModeConstants.ENABLED);
                standby.setOrderNum(1);
                standby.setTenantId(tenantId);
                sysModeService.insertSysMode(standby);

                // 维护模式
                SysMode maintenance = new SysMode();
                maintenance.setModeName("维护模式");
                maintenance.setModeType(ModeConstants.MODE_TYPE_SYSTEM);
                maintenance.setModeColor("#FAAD14");
                maintenance.setModeIcon("fa fa-tools");
                maintenance.setDescription("机器人维护检修状态");
                maintenance.setEnabled(ModeConstants.ENABLED);
                maintenance.setOrderNum(2);
                maintenance.setTenantId(tenantId);
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
                charge.setTenantId(tenantId);
                sysModeService.insertSysMode(charge);

                logger.info("租户 {} 默认模式初始化完成", tenantId);
            }
        } catch (Exception e) {
            logger.error("初始化默认模式失败", e);
        }
    }
}