//package com.ruoyi.function.mapper;
//
//import com.ruoyi.function.TestApplication;
//import com.ruoyi.function.domain.SysPoint;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.math.BigDecimal;
//import java.util.List;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//@SpringBootTest(classes = TestApplication.class)
//@ActiveProfiles("test")
//@Transactional
//@DisplayName("点位Mapper测试")
//class SysPointMapperTest {
//
//    @Autowired
//    private SysPointMapper sysPointMapper;
//
//    @Test
//    @DisplayName("根据地图ID查询点位列表 - 成功")
//    void testSelectByMapId() {
//        List<SysPoint> points = sysPointMapper.selectByMapId(1L);
//        assertThat(points).isNotEmpty();
//        assertThat(points.size()).isEqualTo(2);
//    }
//
//    @Test
//    @DisplayName("根据ID查询点位 - 成功")
//    void testSelectById() {
//        SysPoint point = sysPointMapper.selectById(1L);
//        assertThat(point).isNotNull();
//        assertThat(point.getPointId()).isEqualTo(1L);
//    }
//
//    @Test
//    @DisplayName("查询点位列表 - 成功")
//    void testSelectList() {
//        SysPoint queryParam = new SysPoint();
//        queryParam.setMapId(1L);
//        List<SysPoint> list = sysPointMapper.selectList(queryParam);
//        assertThat(list).isNotEmpty();
//        assertThat(list.size()).isEqualTo(2);
//    }
//
//    @Test
//    @DisplayName("新增点位 - 成功")
//    void testInsert() {
//        SysPoint point = new SysPoint();
//        point.setMapId(1L);
//        point.setPointName("Mapper测试点位");
//        point.setPointCode("MAPPER_001");
//        point.setPointType("normal");
//        point.setCoordinateX(new BigDecimal("50.00"));
//        point.setCoordinateY(new BigDecimal("60.00"));
//        point.setStatus("1");
//        point.setOrderNum(10);
//        point.setDelFlag("0");
//
//        int result = sysPointMapper.insert(point);
//        assertThat(result).isEqualTo(1);
//        assertThat(point.getPointId()).isNotNull();
//    }
//
//    @Test
//    @DisplayName("更新点位 - 成功")
//    void testUpdate() {
//        SysPoint point = sysPointMapper.selectById(1L);
//        assertThat(point).isNotNull();
//
//        point.setPointName("Mapper更新后的点位");
//        int result = sysPointMapper.update(point);
//        assertThat(result).isEqualTo(1);
//    }
//
//    @Test
//    @DisplayName("删除点位 - 成功")
//    void testDeleteById() {
//        int result = sysPointMapper.deleteById(1L);
//        assertThat(result).isEqualTo(1);
//    }
//
//    @Test
//    @DisplayName("根据地图ID删除点位 - 成功")
//    void testDeleteByMapId() {
//        int result = sysPointMapper.deleteByMapId(1L);
//        assertThat(result).isEqualTo(2);
//    }
//
//    @Test
//    @DisplayName("批量删除点位 - 成功")
//    void testDeleteByIds() {
//        Long[] pointIds = {1L, 2L};
//        int result = sysPointMapper.deleteByIds(pointIds);
//        assertThat(result).isEqualTo(2);
//    }
//
//    @Test
//    @DisplayName("统计地图下的点位数量 - 成功")
//    void testCountByMapId() {
//        int count = sysPointMapper.countByMapId(1L);
//        assertThat(count).isEqualTo(2);
//    }
//
//    @Test
//    @DisplayName("检查点位编码唯一性 - 成功")
//    void testCheckPointCodeUnique() {
//        int count = sysPointMapper.checkPointCodeUnique("POINT_001", null);
//        assertThat(count).isGreaterThan(0);
//    }
//}