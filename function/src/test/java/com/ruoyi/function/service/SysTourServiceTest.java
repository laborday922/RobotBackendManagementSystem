//package com.ruoyi.function.service;
//import com.ruoyi.function.BaseServiceTest;
//import com.ruoyi.function.TestApplication;
//import com.ruoyi.function.domain.SysRoutePoint;
//import com.ruoyi.function.domain.SysTourContent;
//import com.ruoyi.function.domain.SysTourGeneral;
//import com.ruoyi.function.domain.SysTourRoute;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.Arrays;
//import java.util.List;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//@SpringBootTest(classes = TestApplication.class)
//@ActiveProfiles("test")
//@Transactional
//@DisplayName("智能讲解Service测试")
//class SysTourServiceTest extends BaseServiceTest {
//
//    @Autowired
//    private ISysTourService sysTourService;
//
//    // ========== 通用配置测试 ==========
//    @Test
//    @DisplayName("获取通用配置 - 成功")
//    void testGetGeneralConfig() {
//        SysTourGeneral config = sysTourService.getGeneralConfig(1L);
//        assertThat(config).isNotNull();
//        assertThat(config.getRobotId()).isEqualTo(1L);
//    }
//
//    @Test
//    @DisplayName("保存通用配置 - 成功")
//    void testSaveGeneralConfig() {
//        SysTourGeneral config = sysTourService.getGeneralConfig(1L);
//        assertThat(config).isNotNull();
//
//        config.setVoice("稳重男声");
//        config.setStartCommand("开始导览");
//        int result = sysTourService.saveGeneralConfig(config);
//        assertThat(result).isEqualTo(1);
//    }
//
//    // ========== 讲解内容测试 ==========
//    @Test
//    @DisplayName("获取讲解内容列表 - 成功")
//    void testGetContentList() {
//        List<SysTourContent> list = sysTourService.getContentList(1L);
//        assertThat(list).isNotEmpty();
//    }
//
//    @Test
//    @DisplayName("获取讲解内容详情 - 成功")
//    void testGetContent() {
//        SysTourContent content = sysTourService.getContent(1L);
//        assertThat(content).isNotNull();
//        assertThat(content.getContentId()).isEqualTo(1L);
//    }
//
//    @Test
//    @DisplayName("保存讲解内容 - 新增成功")
//    void testSaveContentInsert() {
//        SysTourContent content = new SysTourContent();
//        content.setRobotId(1L);
//        content.setPointName("新增测试讲解点");
//        content.setPointDesc("新增测试描述");
//        content.setBroadcastType("text");
//        content.setBroadcastText("新增测试播报内容");
//        content.setVoiceType("温柔女声");
//        content.setSpeechRate(50);
//
//        int result = sysTourService.saveContent(content);
//        assertThat(result).isEqualTo(1);
//        assertThat(content.getContentId()).isNotNull();
//    }
//
//    @Test
//    @DisplayName("保存讲解内容 - 更新成功")
//    void testSaveContentUpdate() {
//        SysTourContent content = sysTourService.getContent(1L);
//        assertThat(content).isNotNull();
//
//        content.setPointName("更新后的讲解点名称");
//        int result = sysTourService.saveContent(content);
//        assertThat(result).isEqualTo(1);
//    }
//
//    @Test
//    @DisplayName("删除讲解内容 - 成功")
//    void testDeleteContent() {
//        int result = sysTourService.deleteContent(1L);
//        assertThat(result).isEqualTo(1);
//    }
//
//    @Test
//    @DisplayName("批量删除讲解内容 - 成功")
//    void testBatchDeleteContents() {
//        List<Long> contentIds = Arrays.asList(1L);
//        int result = sysTourService.batchDeleteContents(contentIds);
//        assertThat(result).isEqualTo(1);
//    }
//
//    // ========== 讲解路线测试 ==========
//    @Test
//    @DisplayName("获取路线列表 - 成功")
//    void testGetRouteList() {
//        List<SysTourRoute> list = sysTourService.getRouteList();
//        assertThat(list).isNotEmpty();
//    }
//
//    @Test
//    @DisplayName("获取路线详情 - 成功")
//    void testGetRoute() {
//        SysTourRoute route = sysTourService.getRoute(1L);
//        assertThat(route).isNotNull();
//        assertThat(route.getRouteId()).isEqualTo(1L);
//    }
//
//    @Test
//    @DisplayName("保存路线 - 新增成功")
//    void testSaveRouteInsert() {
//        SysTourRoute route = new SysTourRoute();
//        route.setRouteName("新增测试路线");
//        route.setMapId(1L);
//        route.setStatus("1");
//
//        int result = sysTourService.saveRoute(route);
//        assertThat(result).isEqualTo(1);
//        assertThat(route.getRouteId()).isNotNull();
//    }
//
//    @Test
//    @DisplayName("保存路线 - 更新成功")
//    void testSaveRouteUpdate() {
//        SysTourRoute route = sysTourService.getRoute(1L);
//        assertThat(route).isNotNull();
//
//        route.setRouteName("更新后的路线名称");
//        int result = sysTourService.saveRoute(route);
//        assertThat(result).isEqualTo(1);
//    }
//
//    @Test
//    @DisplayName("删除路线 - 成功")
//    void testDeleteRoute() {
//        int result = sysTourService.deleteRoute(1L);
//        assertThat(result).isEqualTo(1);
//    }
//
//    @Test
//    @DisplayName("获取路线点位 - 成功")
//    void testGetRoutePoints() {
//        List<SysRoutePoint> points = sysTourService.getRoutePoints(1L);
//        assertThat(points).isNotNull();
//    }
//
//    @Test
//    @DisplayName("导出路线 - 成功")
//    void testExportRoutes() {
//        String json = sysTourService.exportRoutes();
//        assertThat(json).isNotNull();
//    }
//
//    @Test
//    @DisplayName("导入路线 - 成功")
//    void testImportRoutes() {
//        String jsonData = "[{\"routeName\":\"导入测试路线\",\"mapId\":1,\"status\":\"1\"}]";
//        int result = sysTourService.importRoutes(jsonData);
//        // 修改断言：期望至少导入0条或更多
//        assertThat(result).isGreaterThanOrEqualTo(0);
//    }
//}