package com.ruoyi.mode.service;

import com.ruoyi.mode.ModeModuleTestApplication;
import com.ruoyi.mode.domain.SysModeHistory;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = ModeModuleTestApplication.class)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Transactional
public class SysModeHistoryServiceTest {

    @Autowired
    private ISysModeHistoryService historyService;

    private SysModeHistory testHistory;

    @BeforeEach
    void setUp() {
        testHistory = new SysModeHistory();
        testHistory.setOperationTime(new Date());
        testHistory.setOperationType("mode-switch");
        testHistory.setRobotId(1L);
        testHistory.setRobotName("测试机器人");
        testHistory.setModeId(1L);
        testHistory.setModeName("待机模式");
        testHistory.setContent("切换到待机模式");
        testHistory.setOperator("test_user");
        testHistory.setStatus("success");
    }

    @Test
    @Order(1)
    void testInsertSysModeHistory() {
        int result = historyService.insertSysModeHistory(testHistory);
        assertThat(result).isEqualTo(1);
        assertThat(testHistory.getHistoryId()).isNotNull();
    }

    @Test
    @Order(2)
    void testSelectSysModeHistoryById() {
        historyService.insertSysModeHistory(testHistory);
        Long historyId = testHistory.getHistoryId();

        SysModeHistory found = historyService.selectSysModeHistoryById(historyId);
        assertThat(found).isNotNull();
        assertThat(found.getOperationType()).isEqualTo("mode-switch");
    }

    @Test
    @Order(3)
    void testSelectSysModeHistoryList() {
        historyService.insertSysModeHistory(testHistory);

        SysModeHistory query = new SysModeHistory();
        query.setOperationType("mode-switch");
        List<SysModeHistory> list = historyService.selectSysModeHistoryList(query);

        assertThat(list).isNotEmpty();
    }

    @Test
    @Order(4)
    void testSelectHistoryByType() {
        historyService.insertSysModeHistory(testHistory);
        List<SysModeHistory> list = historyService.selectHistoryByType("mode-switch");
        assertThat(list).isNotEmpty();
    }

    @Test
    @Order(5)
    void testSelectHistoryByRobotId() {
        historyService.insertSysModeHistory(testHistory);
        List<SysModeHistory> list = historyService.selectHistoryByRobotId(1L);
        assertThat(list).isNotEmpty();
    }

    @Test
    @Order(6)
    void testSelectRecentHistory() {
        historyService.insertSysModeHistory(testHistory);
        List<SysModeHistory> list = historyService.selectRecentHistory(10);
        assertThat(list).isNotEmpty();
    }

    @Test
    @Order(7)
    void testSelectOperationTypeStats() {
        historyService.insertSysModeHistory(testHistory);
        List<Map<String, Object>> stats = historyService.selectOperationTypeStats();
        assertThat(stats).isNotNull();
    }

    @Test
    @Order(8)
    void testSelectRobotOperationStats() {
        historyService.insertSysModeHistory(testHistory);
        List<Map<String, Object>> stats = historyService.selectRobotOperationStats();
        assertThat(stats).isNotNull();
    }

    @Test
    @Order(9)
    void testSelectDailyOperationStats() {
        historyService.insertSysModeHistory(testHistory);
        List<Map<String, Object>> stats = historyService.selectDailyOperationStats(7);
        assertThat(stats).isNotNull();
    }

    @Test
    @Order(10)
    void testDeleteSysModeHistoryById() {
        historyService.insertSysModeHistory(testHistory);
        int result = historyService.deleteSysModeHistoryById(testHistory.getHistoryId());
        assertThat(result).isEqualTo(1);
    }
}