package com.ruoyi.data.clean.service.impl;

import com.ruoyi.data.clean.domain.CleanExecuteHistory;
import com.ruoyi.data.clean.mapper.CleanExecuteHistoryMapper;
import com.ruoyi.data.clean.service.CleanExecuteHistoryService;
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
        return mapper.selectAll();
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        mapper.updateStatus(id, status);
    }

    @Override
    public void delete(Long id) {
        mapper.deleteById(id);
    }
}