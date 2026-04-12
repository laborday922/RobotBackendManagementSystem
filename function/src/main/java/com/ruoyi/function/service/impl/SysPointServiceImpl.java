package com.ruoyi.function.service.impl;

import com.ruoyi.common.threadlocal.TenantContext;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.function.domain.SysPoint;
import com.ruoyi.function.mapper.SysPointMapper;
import com.ruoyi.function.service.ISysPointService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.ruoyi.common.utils.SecurityUtils.isAdmin;

@Service
public class SysPointServiceImpl implements ISysPointService {

    @Autowired
    private SysPointMapper sysPointMapper;

    @Override
    public SysPoint selectById(Long pointId) {
        return sysPointMapper.selectById(pointId);
    }

    @Override
    public List<SysPoint> selectList(SysPoint point) {
        Long tenantId = TenantContext.get();
        if (!isAdmin(tenantId)) {
            point.setTenantId(tenantId);
        }
        return sysPointMapper.selectList(point);
    }

    @Override
    public int insert(SysPoint point) {
        point.setCreateTime(DateUtils.getNowDate());
        point.setTenantId(TenantContext.get());
        return sysPointMapper.insert(point);
    }

    @Override
    public int update(SysPoint point) {
        point.setUpdateTime(DateUtils.getNowDate());
        return sysPointMapper.update(point);
    }

    @Override
    public int deleteById(Long pointId) {
        return sysPointMapper.deleteById(pointId);
    }

    @Override
    public int deleteByIds(Long[] pointIds) {
        return sysPointMapper.deleteByIds(pointIds);
    }
}