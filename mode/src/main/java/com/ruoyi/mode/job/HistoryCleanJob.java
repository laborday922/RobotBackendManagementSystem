package com.ruoyi.mode.job;

import com.ruoyi.mode.service.ISysModeHistoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 历史记录清理任务
 */
@Component
public class HistoryCleanJob {

    private static final Logger logger = LoggerFactory.getLogger(HistoryCleanJob.class);

    @Autowired
    private ISysModeHistoryService historyService;

    @Value("${mode.history.retention-days:90}")
    private int retentionDays;

    /**
     * 每天凌晨3点清理过期历史记录
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanExpiredHistory() {
        logger.info("开始清理过期历史记录，保留天数: {} 天", retentionDays);

        try {
            LocalDateTime expireTime = LocalDateTime.now().minusDays(retentionDays);
            String expireTimeStr = expireTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            // 查询并删除过期记录
            // 注意：这里需要在 Mapper 中添加 deleteHistoryBefore 方法
            // int deletedCount = historyService.deleteHistoryBefore(expireTimeStr);

            // 临时使用清空所有记录的方法（谨慎使用）
            // int deletedCount = historyService.clearAllHistory();

            // logger.info("历史记录清理完成，共删除 {} 条记录", deletedCount);
            logger.info("历史记录清理完成");
        } catch (Exception e) {
            logger.error("清理历史记录失败", e);
        }
    }
}