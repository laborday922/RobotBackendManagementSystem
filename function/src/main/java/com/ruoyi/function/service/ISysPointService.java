package com.ruoyi.function.service;

import com.ruoyi.function.domain.SysPoint;

import java.util.List;

public interface ISysPointService {

    SysPoint selectById(Long pointId);

    List<SysPoint> selectList(SysPoint point);

    List<SysPoint> selectByMapId(Long mapId);

    // 新增：根据多个地图ID查询点位列表
    List<SysPoint> selectByMapIds(List<Long> mapIds);

    int insert(SysPoint point);

    int update(SysPoint point);

    int deleteById(Long pointId);

    int deleteByIds(Long[] pointIds);
}