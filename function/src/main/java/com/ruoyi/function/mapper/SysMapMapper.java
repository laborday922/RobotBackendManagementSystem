package com.ruoyi.function.mapper;

import com.ruoyi.function.domain.SysMap;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SysMapMapper {

    SysMap selectById(@Param("mapId") Long mapId);

    List<SysMap> selectList(SysMap map);

    int insert(SysMap map);

    int update(SysMap map);

    int deleteById(@Param("mapId") Long mapId);
}