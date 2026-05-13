package com.ruoyi.mode.mapper;

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
public class SysModeHistoryMapperTest {

    @Autowired
    private SysModeHistoryMapper historyMapper;

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
        testHistory.setTenantId(1L);
    }

    @Test
    @Order(1)
    void testInsertSysModeHistory() {
        int result = historyMapper.insertSysModeHistory(testHistory);
        assertThat(result).isEqualTo(1);
        assertThat(testHistory.getHistoryId()).isNotNull();
    }

    @Test
    @Order(2)
    void testSelectSysModeHistoryById() {
        historyMapper.insertSysModeHistory(testHistory);
        Long historyId = testHistory.getHistoryId();

        SysModeHistory found = historyMapper.selectSysModeHistoryById(historyId);
        assertThat(found).isNotNull();
        assertThat(found.getOperationType()).isEqualTo("mode-switch");
        assertThat(found.getContent()).isEqualTo("切换到待机模式");
    }

    @Test
    @Order(3)
    void testSelectSysModeHistoryList() {
        historyMapper.insertSysModeHistory(testHistory);

        SysModeHistory query = new SysModeHistory();
        query.setOperationType("mode-switch");
        List<SysModeHistory> list = historyMapper.selectSysModeHistoryList(query);

        assertThat(list).isNotEmpty();
    }

    @Test
    @Order(4)
    void testSelectSysModeHistoryListByTypes() {
        historyMapper.insertSysModeHistory(testHistory);

        String[] types = {"mode-switch", "emergency"};
        SysModeHistory query = new SysModeHistory();
        List<SysModeHistory> list = historyMapper.selectSysModeHistoryListByTypes(types, query);

        assertThat(list).isNotEmpty();
    }

    @Test
    @Order(5)
    void testUpdateSysModeHistory() {
        historyMapper.insertSysModeHistory(testHistory);
        testHistory.setContent("更新后的内容");
        testHistory.setStatus("warning");

        int result = historyMapper.updateSysModeHistory(testHistory);
        assertThat(result).isEqualTo(1);

        SysModeHistory updated = historyMapper.selectSysModeHistoryById(testHistory.getHistoryId());
        assertThat(updated.getContent()).isEqualTo("更新后的内容");
        assertThat(updated.getStatus()).isEqualTo("warning");
    }

    @Test
    @Order(6)
    void testSelectOperationTypeStats() {
        historyMapper.insertSysModeHistory(testHistory);

        List<Map<String, Object>> stats = historyMapper.selectOperationTypeStats(1L);
        assertThat(stats).isNotNull();
    }

    @Test
    @Order(7)
    void testSelectRobotOperationStats() {
        historyMapper.insertSysModeHistory(testHistory);

        List<Map<String, Object>> stats = historyMapper.selectRobotOperationStats(1L);
        assertThat(stats).isNotNull();
    }

    @Test
    @Order(8)
    void testSelectRecentHistory() {
        historyMapper.insertSysModeHistory(testHistory);

        List<SysModeHistory> recent = historyMapper.selectRecentHistory(10, 1L);
        assertThat(recent).isNotEmpty();
    }

    @Test
    @Order(9)
    void testDeleteSysModeHistoryById() {
        historyMapper.insertSysModeHistory(testHistory);
        int result = historyMapper.deleteSysModeHistoryById(testHistory.getHistoryId());
        assertThat(result).isEqualTo(1);
    }
}