package com.ruoyi.data.clean.service.impl;

import com.ruoyi.data.clean.domain.CleanExecuteHistory;
import com.ruoyi.data.clean.mapper.CleanExecuteHistoryMapper;
import com.ruoyi.data.clean.service.CleanExecuteHistoryService;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CleanExecuteHistoryServiceImpl implements CleanExecuteHistoryService {

    @Resource
    private CleanExecuteHistoryMapper mapper;

    @Override
    public Long createRecord(CleanExecuteHistory history) {
        history.setCreateTime(LocalDateTime.now());
        mapper.insert(history);
        return history.getId();
    }

    @Override
    public CleanExecuteHistory getById(Long id) {
        return mapper.selectById(id);
    }

    @Override
    public List<CleanExecuteHistory> listAll() {
        List<CleanExecuteHistory> list = mapper.selectAll();
        for (CleanExecuteHistory item : list) {
            item.setNextRunTime(calcNextRunTime(item.getCronExpression()));
        }
        return list;
    }

    /**
     * 根据 cron 表达式计算下次执行时间
     */
    private LocalDateTime calcNextRunTime(String cronExpression) {
        if (cronExpression == null || cronExpression.isEmpty()) {
            return null;
        }
        try {
            return CronExpression.parse(cronExpression).next(LocalDateTime.now());
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void update(CleanExecuteHistory history) {
        mapper.update(history);
    }

    @Override
    public void delete(Long id) {
        mapper.deleteById(id);
    }
}
