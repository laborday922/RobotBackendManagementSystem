package com.ruoyi.function.mapper;

import com.ruoyi.function.TestApplication;
import com.ruoyi.function.domain.SysNavConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = TestApplication.class)
@ActiveProfiles("test")
@Transactional
@DisplayName("导航配置Mapper测试")
class SysNavConfigMapperTest {

    @Autowired
    private SysNavConfigMapper sysNavConfigMapper;

    @Test
    @DisplayName("获取当前配置 - 成功")
    void testSelectCurrent() {
        SysNavConfig config = sysNavConfigMapper.selectCurrent();
        assertThat(config).isNotNull();
    }

    @Test
    @DisplayName("根据机器人ID获取配置 - 成功")
    void testSelectByRobotId() {
        SysNavConfig config = sysNavConfigMapper.selectByRobotId("robot_001");
        assertThat(config).isNotNull();
        assertThat(config.getRobotId()).isEqualTo("robot_001");
    }

    @Test
    @DisplayName("新增配置 - 成功")
    void testInsert() {
        SysNavConfig config = new SysNavConfig();
        config.setRobotId("new_robot_mapper");
        config.setMapId(1L);
        config.setVoiceType("default");

        int result = sysNavConfigMapper.insert(config);
        assertThat(result).isEqualTo(1);
        assertThat(config.getNavId()).isNotNull();
    }

    @Test
    @DisplayName("更新配置 - 成功")
    void testUpdate() {
        SysNavConfig config = sysNavConfigMapper.selectByRobotId("robot_001");
        assertThat(config).isNotNull();

        config.setVoiceType("custom");
        int result = sysNavConfigMapper.update(config);
        assertThat(result).isEqualTo(1);
    }

    @Test
    @DisplayName("根据机器人ID更新配置 - 成功")
    void testUpdateByRobotId() {
        SysNavConfig config = new SysNavConfig();
        config.setRobotId("robot_001");
        config.setVoiceType("custom");
        config.setBeforeMsg("Mapper更新的播报语");

        int result = sysNavConfigMapper.updateByRobotId(config);
        assertThat(result).isEqualTo(1);
    }
}