package com.ruoyi.function.service;
import com.ruoyi.function.TestApplication;
import com.ruoyi.function.domain.SysReceptionConfig;
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
@DisplayName("业务接待配置Service测试")
class SysReceptionConfigServiceTest extends BaseServiceTest{

    @Autowired
    private ISysReceptionConfigService sysReceptionConfigService;

    @Test
    @DisplayName("根据机器人ID获取配置 - 成功")
    void testGetConfigByRobotId() {
        SysReceptionConfig config = sysReceptionConfigService.getConfigByRobotId(1L);
        assertThat(config).isNotNull();
        assertThat(config.getRobotId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("保存配置 - 新增成功")
    void testSaveConfigInsert() {
        SysReceptionConfig config = new SysReceptionConfig();
        config.setRobotId(100L);
        config.setWelcome("新增欢迎语");
        config.setRepeat("新增重复语");
        config.setIdle("新增未唤醒语");
        config.setVipEnabled("1");
        config.setVipGreeting("新增VIP语");
        config.setVipMulti("新增多VIP语");
        config.setStrangerEnabled("1");
        config.setStrangerGreeting("新增陌生人语");
        config.setStrangerMulti("新增多陌生人语");

        int result = sysReceptionConfigService.saveConfig(config);
        assertThat(result).isEqualTo(1);
    }

    @Test
    @DisplayName("保存配置 - 更新成功")
    void testSaveConfigUpdate() {
        SysReceptionConfig config = sysReceptionConfigService.getConfigByRobotId(1L);
        assertThat(config).isNotNull();

        config.setWelcome("更新后的欢迎语");
        int result = sysReceptionConfigService.saveConfig(config);
        assertThat(result).isEqualTo(1);
    }

    @Test
    @DisplayName("重置为默认配置 - 成功")
    void testResetToDefault() {
        int result = sysReceptionConfigService.resetToDefault(1L);
        assertThat(result).isEqualTo(1);
    }
}