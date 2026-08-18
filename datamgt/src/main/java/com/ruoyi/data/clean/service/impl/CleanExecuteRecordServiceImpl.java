package com.ruoyi.data.clean.service.impl;

import com.ruoyi.common.threadlocal.TenantContext;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.data.clean.domain.CleanExecuteRecord;
import com.ruoyi.data.clean.mapper.CleanExecuteRecordMapper;
import com.ruoyi.data.clean.service.CleanExecuteRecordService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class CleanExecuteRecordServiceImpl implements CleanExecuteRecordService {

    @Resource
    private CleanExecuteRecordMapper mapper;

    private Long getQueryTenantId() {
        Long tenantId = TenantContext.get();
        Long userId = SecurityUtils.getUserId();
        boolean isAdmin = SecurityUtils.isAdmin(userId);
        return isAdmin ? null : tenantId;
    }

    @Override
    public List<CleanExecuteRecord> listAll() {
        return mapper.selectAll(getQueryTenantId());
    }
}
