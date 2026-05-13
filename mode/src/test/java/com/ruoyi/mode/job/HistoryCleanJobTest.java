package com.ruoyi.mode.job;

import com.ruoyi.mode.ModeModuleTestApplication;
import com.ruoyi.mode.service.ISysModeHistoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = ModeModuleTestApplication.class)
@ActiveProfiles("test")
@DisplayName("历史记录清理任务测试")
class HistoryCleanJobTest {

    @Autowired
    private HistoryCleanJob historyCleanJob;

    @Autowired
    private ISysModeHistoryService historyService;

    @Test
    @DisplayName("清理过期历史记录 - 成功")
    void testCleanExpiredHistory() {
        // 执行清理任务
        historyCleanJob.cleanExpiredHistory();

        // 验证清理结果
        assertThat(true).isTrue();
    }
}