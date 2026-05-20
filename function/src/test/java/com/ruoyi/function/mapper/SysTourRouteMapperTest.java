//package com.ruoyi.function.mapper;
//
//import com.ruoyi.function.TestApplication;
//import com.ruoyi.function.domain.SysRoutePoint;
//import com.ruoyi.function.domain.SysTourRoute;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//@SpringBootTest(classes = TestApplication.class)
//@ActiveProfiles("test")
//@Transactional
//@DisplayName("讲解路线Mapper测试")
//class SysTourRouteMapperTest {
//
//    @Autowired
//    private SysTourRouteMapper sysTourRouteMapper;
//
//    @Test
//    @DisplayName("根据ID查询路线 - 成功")
//    void testSelectById() {
//        SysTourRoute route = sysTourRouteMapper.selectById(1L);
//        assertThat(route).isNotNull();
//        assertThat(route.getRouteId()).isEqualTo(1L);
//    }
//
//    @Test
//    @DisplayName("查询路线列表 - 成功")
//    void testSelectList() {
//        SysTourRoute queryParam = new SysTourRoute();
//        List<SysTourRoute> list = sysTourRouteMapper.selectList(queryParam);
//        assertThat(list).isNotEmpty();
//    }
//
//    @Test
//    @DisplayName("新增路线 - 成功")
//    void testInsert() {
//        SysTourRoute route = new SysTourRoute();
//        route.setRouteName("Mapper测试路线");
//        route.setMapId(1L);
//        route.setStatus("1");
//
//        int result = sysTourRouteMapper.insert(route);
//        assertThat(result).isEqualTo(1);
//        assertThat(route.getRouteId()).isNotNull();
//    }
//
//    @Test
//    @DisplayName("更新路线 - 成功")
//    void testUpdate() {
//        SysTourRoute route = sysTourRouteMapper.selectById(1L);
//        assertThat(route).isNotNull();
//
//        route.setRouteName("Mapper更新后的路线");
//        int result = sysTourRouteMapper.update(route);
//        assertThat(result).isEqualTo(1);
//    }
//
//    @Test
//    @DisplayName("删除路线 - 成功")
//    void testDeleteById() {
//        int result = sysTourRouteMapper.deleteById(1L);
//        assertThat(result).isEqualTo(1);
//    }
//
//    @Test
//    @DisplayName("查询路线点位 - 成功")
//    void testSelectRoutePoints() {
//        List<SysRoutePoint> points = sysTourRouteMapper.selectRoutePoints(1L);
//        assertThat(points).isNotNull();
//    }
//
//    @Test
//    @DisplayName("新增路线点位 - 成功")
//    void testInsertRoutePoint() {
//        SysRoutePoint routePoint = new SysRoutePoint();
//        routePoint.setRouteId(1L);
//        routePoint.setPointId(1L);
//        routePoint.setOrderNum(1);
//
//        int result = sysTourRouteMapper.insertRoutePoint(routePoint);
//        assertThat(result).isEqualTo(1);
//        assertThat(routePoint.getId()).isNotNull();
//    }
//
//    @Test
//    @DisplayName("删除路线点位 - 成功")
//    void testDeleteRoutePoints() {
//        int result = sysTourRouteMapper.deleteRoutePoints(1L);
//        assertThat(result).isGreaterThanOrEqualTo(0);
//    }
//
//    @Test
//    @DisplayName("批量新增路线点位 - 成功")
//    void testBatchInsertRoutePoints() {
//        List<SysRoutePoint> points = new ArrayList<>();
//        SysRoutePoint point1 = new SysRoutePoint();
//        point1.setPointId(1L);
//        point1.setOrderNum(1);
//        points.add(point1);
//
//        int result = sysTourRouteMapper.batchInsertRoutePoints(1L, points);
//        assertThat(result).isEqualTo(1);
//    }
//}