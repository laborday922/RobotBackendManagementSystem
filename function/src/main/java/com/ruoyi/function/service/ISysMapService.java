package com.ruoyi.function.service;

import com.ruoyi.function.domain.SysMap;
import com.ruoyi.function.domain.SysPoint;

import java.util.List;

public interface ISysMapService {

    SysMap selectById(Long mapId);

    List<SysMap> selectList(SysMap map);

    int insert(SysMap map);

    int update(SysMap map);

    int deleteById(Long mapId);

    List<SysPoint> selectPointsByMapId(Long mapId);
}