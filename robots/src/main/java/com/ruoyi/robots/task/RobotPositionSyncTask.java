package com.ruoyi.robots.task;

import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.robots.domain.RobotPositionHistory;
import com.ruoyi.robots.mapper.RobotPositionHistoryMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.Collection;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class RobotPositionSyncTask {

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private RobotPositionHistoryMapper robotPositionHistoryMapper;

    /**
     * 每小时执行一次
     */
    @Scheduled(cron = "0 0 */1 * * ?")
    public void syncRobotPositionToDb() {
        log.info("===== 开始每小时同步机器人位置数据到数据库 =====");

        // 1. 获取所有机器人的 Redis key
        Collection<String> keys = redisCache.keys("robot:position:*");
        if (keys == null || keys.isEmpty()) {
            log.info("暂无机器人位置数据需要同步");
            return;
        }

        List<RobotPositionHistory> historyList = new ArrayList<>();
        // 2. 从 Redis 取出所有机器人数据
        for (String key : keys) {
            RobotPositionHistory history = redisCache.getCacheObject(key);
            if (history != null) {
                historyList.add(history);
            }
        }
//todo
        // 3. 循环单条插入（适配你现有的 insert 方法）
        int successCount = 0;
        for (RobotPositionHistory history : historyList) {
            try {
                history.setRecordTime(DateUtils.getNowDate());
                robotPositionHistoryMapper.insertRobotPositionHistory(history);
                successCount++;
            } catch (Exception e) {
                log.error("插入机器人位置数据失败", e);
            }
        }

        log.info("===== 同步完成，成功同步 {} 条机器人位置数据 =====", successCount);
    }
}