package com.ruoyi.function.mapper;

import com.ruoyi.function.domain.SysRoutePoint;
import com.ruoyi.function.domain.SysTourRoute;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SysTourRouteMapper {

    SysTourRoute selectById(@Param("routeId") Long routeId);

    List<SysTourRoute> selectList(SysTourRoute route);

    int insert(SysTourRoute route);

    int update(SysTourRoute route);

    int deleteById(@Param("routeId") Long routeId);

    // 路线点位关联操作
    List<SysRoutePoint> selectRoutePoints(@Param("routeId") Long routeId);

    int insertRoutePoint(SysRoutePoint routePoint);

    int deleteRoutePoints(@Param("routeId") Long routeId);

    int batchInsertRoutePoints(@Param("routeId") Long routeId, @Param("points") List<SysRoutePoint> points);
}
