package com.ruoyi.function.service.impl;

import com.ruoyi.common.threadlocal.TenantContext;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.function.domain.SysMap;
import com.ruoyi.function.domain.SysPoint;
import com.ruoyi.function.mapper.SysMapMapper;
import com.ruoyi.function.mapper.SysPointMapper;
import com.ruoyi.function.service.ISysMapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.ruoyi.common.utils.SecurityUtils.isAdmin;

@Service
public class SysMapServiceImpl implements ISysMapService {

    @Autowired
    private SysMapMapper sysMapMapper;

    @Autowired
    private SysPointMapper sysPointMapper;

    @Override
    public SysMap selectById(Long mapId) {
        return sysMapMapper.selectById(mapId);
    }

    @Override
    public List<SysMap> selectList(SysMap map) {
        Long tenantId = TenantContext.get();
        if (!isAdmin(tenantId)) {
            map.setTenantId(tenantId);
        }
        return sysMapMapper.selectList(map);
    }

    @Override
    public int insert(SysMap map) {
        map.setCreateTime(DateUtils.getNowDate());
        map.setTenantId(TenantContext.get());
        return sysMapMapper.insert(map);
    }

    @Override
    public int update(SysMap map) {
        map.setUpdateTime(DateUtils.getNowDate());
        return sysMapMapper.update(map);
    }

    @Override
    public int deleteById(Long mapId) {
        return sysMapMapper.deleteById(mapId);
    }

    @Override
    public List<SysPoint> selectPointsByMapId(Long mapId) {
        return sysPointMapper.selectByMapId(mapId);
    }
}