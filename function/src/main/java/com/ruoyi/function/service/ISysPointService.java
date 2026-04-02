package com.ruoyi.function.service;

import com.ruoyi.function.domain.SysPoint;

import java.util.List;

public interface ISysPointService {

    SysPoint selectById(Long pointId);

    List<SysPoint> selectList(SysPoint point);

    int insert(SysPoint point);

    int update(SysPoint point);

    int deleteById(Long pointId);

    int deleteByIds(Long[] pointIds);
}