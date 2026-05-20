package com.ruoyi.function.mapper;

import com.ruoyi.function.TestApplication;
import com.ruoyi.function.domain.SysMap;
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
@DisplayName("地图Mapper测试")
class SysMapMapperTest {

    @Autowired
    private SysMapMapper sysMapMapper;

    @Test
    @DisplayName("根据ID查询地图 - 成功")
    void testSelectById() {
        SysMap map = sysMapMapper.selectById(1L);
        assertThat(map).isNotNull();
        assertThat(map.getMapId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("查询地图列表 - 成功")
    void testSelectList() {
        SysMap queryParam = new SysMap();
        queryParam.setRobotId("robot_001");
        List<SysMap> list = sysMapMapper.selectList(queryParam);
        assertThat(list).isNotEmpty();
    }

    @Test
    @DisplayName("新增地图 - 成功")
    void testInsert() {
        SysMap map = new SysMap();
        map.setMapName("Mapper测试地图");
        map.setRobotId("robot_001");
        map.setStatus("1");
        map.setDelFlag("0");

        int result = sysMapMapper.insert(map);
        assertThat(result).isEqualTo(1);
        assertThat(map.getMapId()).isNotNull();
    }

    @Test
    @DisplayName("更新地图 - 成功")
    void testUpdate() {
        SysMap map = sysMapMapper.selectById(1L);
        assertThat(map).isNotNull();

        map.setMapName("Mapper更新后的名称");
        int result = sysMapMapper.update(map);
        assertThat(result).isEqualTo(1);
    }

    @Test
    @DisplayName("删除地图 - 成功")
    void testDeleteById() {
        int result = sysMapMapper.deleteById(1L);
        assertThat(result).isEqualTo(1);
    }
}