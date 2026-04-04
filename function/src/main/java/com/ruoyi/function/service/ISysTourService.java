package com.ruoyi.function.service;

import com.ruoyi.function.domain.SysRoutePoint;
import com.ruoyi.function.domain.SysTourContent;
import com.ruoyi.function.domain.SysTourGeneral;
import com.ruoyi.function.domain.SysTourRoute;

import java.util.List;

public interface ISysTourService {

    // 通用配置
    SysTourGeneral getGeneralConfig(Long robotId);
    int saveGeneralConfig(SysTourGeneral config);

    // 讲解内容
    List<SysTourContent> getContentList(Long robotId);
    SysTourContent getContent(Long contentId);
    int saveContent(SysTourContent content);
    int deleteContent(Long contentId);
    int batchDeleteContents(List<Long> contentIds);

    // 讲解路线
    List<SysTourRoute> getRouteList();
    SysTourRoute getRoute(Long routeId);
    int saveRoute(SysTourRoute route);
    int deleteRoute(Long routeId);
    int deleteRouteById(Long routeId);

    // 路线点位配置
    List<SysRoutePoint> getRoutePoints(Long routeId);
    int saveRoutePoints(Long routeId, List<SysRoutePoint> points);

    // 导入导出
    String exportRoutes();
    int importRoutes(String jsonData);
}