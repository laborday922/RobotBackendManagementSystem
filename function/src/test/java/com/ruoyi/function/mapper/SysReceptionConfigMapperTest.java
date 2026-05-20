package com.ruoyi.function.mapper;

import com.ruoyi.function.TestApplication;
import com.ruoyi.function.domain.SysReceptionConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = TestApplication.class)
@ActiveProfiles("test")
@Transactional
@DisplayName("业务接待配置Mapper测试")
class SysReceptionConfigMapperTest {

    @Autowired
    private SysReceptionConfigMapper sysReceptionConfigMapper;

    @Test
    @DisplayName("根据机器人ID获取配置 - 成功")
    void testSelectByRobotId() {
        SysReceptionConfig config = sysReceptionConfigMapper.selectByRobotId(1L);
        assertThat(config).isNotNull();
        assertThat(config.getRobotId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("查询配置列表 - 成功")
    void testSelectList() {
        SysReceptionConfig queryParam = new SysReceptionConfig();
        List<SysReceptionConfig> list = sysReceptionConfigMapper.selectList(queryParam);
        assertThat(list).isNotEmpty();
    }

    @Test
    @DisplayName("新增配置 - 成功")
    void testInsert() {
        SysReceptionConfig config = new SysReceptionConfig();
        config.setRobotId(100L);
        config.setWelcome("Mapper欢迎语");
        config.setRepeat("Mapper重复语");

        int result = sysReceptionConfigMapper.insert(config);
        assertThat(result).isEqualTo(1);
        assertThat(config.getConfigId()).isNotNull();
    }

    @Test
    @DisplayName("更新配置 - 成功")
    void testUpdate() {
        SysReceptionConfig config = sysReceptionConfigMapper.selectByRobotId(1L);
        assertThat(config).isNotNull();

        config.setWelcome("Mapper更新后的欢迎语");
        int result = sysReceptionConfigMapper.update(config);
        assertThat(result).isEqualTo(1);
    }

    @Test
    @DisplayName("删除配置 - 成功")
    void testDeleteByRobotId() {
        int result = sysReceptionConfigMapper.deleteByRobotId(1L);
        assertThat(result).isEqualTo(1);
    }
}