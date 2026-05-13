package com.ruoyi.function.mapper;


import com.ruoyi.function.TestApplication;
import com.ruoyi.function.domain.SysTourGeneral;
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
@DisplayName("讲解通用配置Mapper测试")
class SysTourGeneralMapperTest {

    @Autowired
    private SysTourGeneralMapper sysTourGeneralMapper;

    @Test
    @DisplayName("根据机器人ID查询配置 - 成功")
    void testSelectByRobotId() {
        SysTourGeneral config = sysTourGeneralMapper.selectByRobotId(1L);
        assertThat(config).isNotNull();
        assertThat(config.getRobotId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("查询配置列表 - 成功")
    void testSelectList() {
        SysTourGeneral queryParam = new SysTourGeneral();
        List<SysTourGeneral> list = sysTourGeneralMapper.selectList(queryParam);
        assertThat(list).isNotEmpty();
    }

    @Test
    @DisplayName("新增配置 - 成功")
    void testInsert() {
        SysTourGeneral config = new SysTourGeneral();
        config.setRobotId(100L);
        config.setVoice("温柔女声");
        config.setVoiceInteraction("1");

        int result = sysTourGeneralMapper.insert(config);
        assertThat(result).isEqualTo(1);
        assertThat(config.getConfigId()).isNotNull();
    }

    @Test
    @DisplayName("更新配置 - 成功")
    void testUpdate() {
        SysTourGeneral config = sysTourGeneralMapper.selectByRobotId(1L);
        assertThat(config).isNotNull();

        config.setVoice("稳重男声");
        int result = sysTourGeneralMapper.update(config);
        assertThat(result).isEqualTo(1);
    }

    @Test
    @DisplayName("删除配置 - 成功")
    void testDeleteByRobotId() {
        int result = sysTourGeneralMapper.deleteByRobotId(1L);
        assertThat(result).isEqualTo(1);
    }
}