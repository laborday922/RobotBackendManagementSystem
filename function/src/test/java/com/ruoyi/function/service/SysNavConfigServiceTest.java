package com.ruoyi.function.service;
import com.ruoyi.function.TestApplication;
import com.ruoyi.function.domain.SysNavConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.function.BaseServiceTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = TestApplication.class)
@ActiveProfiles("test")
@Transactional
@DisplayName("导航配置Service测试")
class SysNavConfigServiceTest extends BaseServiceTest{

    @Autowired
    private ISysNavConfigService sysNavConfigService;

    @Test
    @DisplayName("获取当前配置 - 成功")
    void testGetCurrentConfig() {
        SysNavConfig config = sysNavConfigService.getCurrentConfig();
        assertThat(config).isNotNull();
    }

    @Test
    @DisplayName("根据机器人ID获取配置 - 成功")
    void testGetConfigByRobotId() {
        SysNavConfig config = sysNavConfigService.getConfigByRobotId("robot_001");
        assertThat(config).isNotNull();
        assertThat(config.getRobotId()).isEqualTo("robot_001");
    }

    @Test
    @DisplayName("根据机器人ID获取配置 - 不存在")
    void testGetConfigByRobotIdNotFound() {
        SysNavConfig config = sysNavConfigService.getConfigByRobotId("not_exist_robot");
        assertThat(config).isNull();
    }

    @Test
    @DisplayName("保存配置 - 新增成功")
    void testSaveConfigInsert() {
        SysNavConfig config = new SysNavConfig();
        config.setRobotId("new_robot_001");
        config.setMapId(1L);
        config.setVoiceType("custom");
        config.setBeforeMsg("新增出发前播报");
        config.setDuringMsg("新增导航中播报");
        config.setAfterMsg("新增到达后播报");

        int result = sysNavConfigService.saveConfig(config);
        assertThat(result).isEqualTo(1);
    }

    @Test
    @DisplayName("保存配置 - 更新成功")
    void testSaveConfigUpdate() {
        SysNavConfig config = sysNavConfigService.getConfigByRobotId("robot_001");
        assertThat(config).isNotNull();

        config.setVoiceType("custom");
        config.setBeforeMsg("更新后的出发前播报");
        int result = sysNavConfigService.saveConfig(config);
        assertThat(result).isEqualTo(1);
    }

    @Test
    @DisplayName("开始导航 - 成功")
    void testStartNavigation() {
        int result = sysNavConfigService.startNavigation("测试点位1");
        assertThat(result).isEqualTo(1);
    }

    @Test
    @DisplayName("紧急停止 - 成功")
    void testEmergencyStop() {
        int result = sysNavConfigService.emergencyStop();
        assertThat(result).isEqualTo(1);
    }
}