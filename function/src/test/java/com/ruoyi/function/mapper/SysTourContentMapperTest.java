package com.ruoyi.function.mapper;

import com.ruoyi.function.TestApplication;
import com.ruoyi.function.domain.SysTourContent;
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
@DisplayName("讲解内容Mapper测试")
class SysTourContentMapperTest {

    @Autowired
    private SysTourContentMapper sysTourContentMapper;

    @Test
    @DisplayName("根据ID查询讲解内容 - 成功")
    void testSelectById() {
        SysTourContent content = sysTourContentMapper.selectById(1L);
        assertThat(content).isNotNull();
        assertThat(content.getContentId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("根据机器人ID查询讲解内容列表 - 成功")
    void testSelectByRobotId() {
        List<SysTourContent> list = sysTourContentMapper.selectByRobotId(1L);
        assertThat(list).isNotEmpty();
    }

    @Test
    @DisplayName("查询讲解内容列表 - 成功")
    void testSelectList() {
        SysTourContent queryParam = new SysTourContent();
        queryParam.setRobotId(1L);
        List<SysTourContent> list = sysTourContentMapper.selectList(queryParam);
        assertThat(list).isNotEmpty();
    }

    @Test
    @DisplayName("新增讲解内容 - 成功")
    void testInsert() {
        SysTourContent content = new SysTourContent();
        content.setRobotId(1L);
        content.setPointName("Mapper测试讲解点");
        content.setBroadcastType("text");
        content.setBroadcastText("Mapper测试内容");

        int result = sysTourContentMapper.insert(content);
        assertThat(result).isEqualTo(1);
        assertThat(content.getContentId()).isNotNull();
    }

    @Test
    @DisplayName("更新讲解内容 - 成功")
    void testUpdate() {
        SysTourContent content = sysTourContentMapper.selectById(1L);
        assertThat(content).isNotNull();

        content.setPointName("Mapper更新后的讲解点");
        int result = sysTourContentMapper.update(content);
        assertThat(result).isEqualTo(1);
    }

    @Test
    @DisplayName("删除讲解内容 - 成功")
    void testDeleteById() {
        int result = sysTourContentMapper.deleteById(1L);
        assertThat(result).isEqualTo(1);
    }
}