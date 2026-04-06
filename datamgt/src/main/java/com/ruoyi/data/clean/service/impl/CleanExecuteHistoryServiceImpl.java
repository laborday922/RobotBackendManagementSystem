package com.ruoyi.data.clean.service.impl;

import com.ruoyi.common.threadlocal.TenantContext;
import com.ruoyi.common.utils.SecurityUtils;
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

    /**
     * 动态获取当前租户ID（根据用户权限决定是否过滤）
     */
    private Long getQueryTenantId() {
        Long tenantId = TenantContext.get();
        Long userId = SecurityUtils.getUserId();
        boolean isAdmin = SecurityUtils.isAdmin(userId);
        return isAdmin ? null : tenantId;
    }

    @Override
    public CleanExecuteHistory getById(Long id) {
        return mapper.selectById(id, getQueryTenantId());
    }

    @Override
    public List<CleanExecuteHistory> listAll() {
        return mapper.selectAll(getQueryTenantId());
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        mapper.updateStatus(id, status, getQueryTenantId());
    }

    @Override
    public void delete(Long id) {
        mapper.deleteById(id, getQueryTenantId());
    }
}