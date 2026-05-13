//package com.ruoyi.function.service;
//import com.ruoyi.function.TestApplication;
//import com.ruoyi.function.domain.SysPoint;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.transaction.annotation.Transactional;
//import com.ruoyi.function.BaseServiceTest;
//
//import java.math.BigDecimal;
//import java.util.List;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//@SpringBootTest(classes = TestApplication.class)
//@ActiveProfiles("test")
//@Transactional
//@DisplayName("点位Service测试")
//class SysPointServiceTest extends BaseServiceTest{
//
//    @Autowired
//    private ISysPointService sysPointService;
//
//    @Test
//    @DisplayName("根据ID查询点位 - 成功")
//    void testSelectById() {
//        SysPoint point = sysPointService.selectById(1L);
//        assertThat(point).isNotNull();
//        assertThat(point.getPointId()).isEqualTo(1L);
//        assertThat(point.getPointName()).isEqualTo("测试点位1");
//    }
//
//    @Test
//    @DisplayName("查询点位列表 - 成功")
//    void testSelectList() {
//        SysPoint queryParam = new SysPoint();
//        queryParam.setMapId(1L);
//        List<SysPoint> list = sysPointService.selectList(queryParam);
//        assertThat(list).isNotEmpty();
//        assertThat(list.size()).isEqualTo(2);
//    }
//
//    @Test
//    @DisplayName("新增点位 - 成功")
//    void testInsert() {
//        SysPoint point = new SysPoint();
//        point.setMapId(1L);
//        point.setPointName("新增测试点位");
//        point.setPointCode("NEW_001");
//        point.setPointType("normal");
//        point.setCoordinateX(new BigDecimal("100.00"));
//        point.setCoordinateY(new BigDecimal("200.00"));
//        point.setStatus("1");
//        point.setOrderNum(10);
//
//        int result = sysPointService.insert(point);
//        assertThat(result).isEqualTo(1);
//        assertThat(point.getPointId()).isNotNull();
//    }
//
//    @Test
//    @DisplayName("更新点位 - 成功")
//    void testUpdate() {
//        SysPoint point = sysPointService.selectById(1L);
//        assertThat(point).isNotNull();
//
//        String newName = "更新后的点位名称";
//        point.setPointName(newName);
//        int result = sysPointService.update(point);
//        assertThat(result).isEqualTo(1);
//
//        SysPoint updatedPoint = sysPointService.selectById(1L);
//        assertThat(updatedPoint.getPointName()).isEqualTo(newName);
//    }
//
//    @Test
//    @DisplayName("删除点位 - 成功")
//    void testDeleteById() {
//        int result = sysPointService.deleteById(1L);
//        assertThat(result).isEqualTo(1);
//
//        SysPoint deletedPoint = sysPointService.selectById(1L);
//        assertThat(deletedPoint).isNull();
//    }
//
//    @Test
//    @DisplayName("批量删除点位 - 成功")
//    void testDeleteByIds() {
//        Long[] pointIds = {1L, 2L};
//        int result = sysPointService.deleteByIds(pointIds);
//        assertThat(result).isEqualTo(2);
//    }
//}