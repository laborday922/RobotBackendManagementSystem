//package com.ruoyi.function.service;
//import com.ruoyi.function.TestApplication;
//import com.ruoyi.function.domain.SysMap;
//import com.ruoyi.function.domain.SysPoint;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.transaction.annotation.Transactional;
//import com.ruoyi.function.BaseServiceTest;
//
//import java.util.List;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//@SpringBootTest(classes = TestApplication.class)
//@ActiveProfiles("test")
//@Transactional
//@DisplayName("地图Service测试")
//class SysMapServiceTest extends BaseServiceTest{
//
//    @Autowired
//    private ISysMapService sysMapService;
//
//    @Test
//    @DisplayName("根据ID查询地图 - 成功")
//    void testSelectById() {
//        SysMap map = sysMapService.selectById(1L);
//        assertThat(map).isNotNull();
//        assertThat(map.getMapId()).isEqualTo(1L);
//        assertThat(map.getMapName()).isEqualTo("测试地图1");
//    }
//
//    @Test
//    @DisplayName("根据ID查询地图 - 不存在")
//    void testSelectByIdNotFound() {
//        SysMap map = sysMapService.selectById(999L);
//        assertThat(map).isNull();
//    }
//
//    @Test
//    @DisplayName("查询地图列表 - 成功")
//    void testSelectList() {
//        SysMap queryParam = new SysMap();
//        queryParam.setRobotId("robot_001");
//        List<SysMap> list = sysMapService.selectList(queryParam);
//        assertThat(list).isNotEmpty();
//        assertThat(list.size()).isGreaterThanOrEqualTo(2);
//    }
//
//    @Test
//    @DisplayName("新增地图 - 成功")
//    void testInsert() {
//        SysMap map = new SysMap();
//        map.setMapName("新增测试地图");
//        map.setRobotId("robot_001");
//        map.setStatus("1");
//
//        int result = sysMapService.insert(map);
//        assertThat(result).isEqualTo(1);
//        assertThat(map.getMapId()).isNotNull();
//    }
//
//    @Test
//    @DisplayName("更新地图 - 成功")
//    void testUpdate() {
//        SysMap map = sysMapService.selectById(1L);
//        assertThat(map).isNotNull();
//
//        String newName = "更新后的地图名称";
//        map.setMapName(newName);
//        int result = sysMapService.update(map);
//        assertThat(result).isEqualTo(1);
//
//        SysMap updatedMap = sysMapService.selectById(1L);
//        assertThat(updatedMap.getMapName()).isEqualTo(newName);
//    }
//
//    @Test
//    @DisplayName("删除地图 - 成功")
//    void testDeleteById() {
//        int result = sysMapService.deleteById(1L);
//        assertThat(result).isEqualTo(1);
//
//        SysMap deletedMap = sysMapService.selectById(1L);
//        assertThat(deletedMap).isNull();
//    }
//
//    @Test
//    @DisplayName("根据地图ID查询点位列表 - 成功")
//    void testSelectPointsByMapId() {
//        List<SysPoint> points = sysMapService.selectPointsByMapId(1L);
//        assertThat(points).isNotEmpty();
//        assertThat(points.size()).isEqualTo(2);
//    }
//}