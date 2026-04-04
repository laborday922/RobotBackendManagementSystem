package com.ruoyi.function.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.function.domain.SysRoutePoint;
import com.ruoyi.function.domain.SysTourContent;
import com.ruoyi.function.domain.SysTourGeneral;
import com.ruoyi.function.domain.SysTourRoute;
import com.ruoyi.function.mapper.SysTourContentMapper;
import com.ruoyi.function.mapper.SysTourGeneralMapper;
import com.ruoyi.function.mapper.SysTourRouteMapper;
import com.ruoyi.function.service.ISysTourService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SysTourServiceImpl implements ISysTourService {

    @Autowired
    private SysTourGeneralMapper tourGeneralMapper;

    @Autowired
    private SysTourContentMapper tourContentMapper;

    @Autowired
    private SysTourRouteMapper tourRouteMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public SysTourGeneral getGeneralConfig(Long robotId) {
        SysTourGeneral config = tourGeneralMapper.selectByRobotId(robotId);
        if (config == null) {
            config = new SysTourGeneral();
            config.setRobotId(robotId);
            config.setVoice("温柔女声");
            config.setVoiceInteraction("1");
            config.setStartCommand("开始讲解");
            config.setBeforeTip("即将开始讲解，请跟随我");
            config.setEndTip("本次讲解结束，谢谢");
            config.setAfterAction("stay");
            tourGeneralMapper.insert(config);
        }
        return config;
    }

    @Override
    public int saveGeneralConfig(SysTourGeneral config) {
        config.setUpdateTime(DateUtils.getNowDate());
        SysTourGeneral existing = tourGeneralMapper.selectByRobotId(config.getRobotId());
        if (existing != null) {
            config.setConfigId(existing.getConfigId());
            return tourGeneralMapper.update(config);
        } else {
            config.setCreateTime(DateUtils.getNowDate());
            return tourGeneralMapper.insert(config);
        }
    }

    @Override
    public List<SysTourContent> getContentList(Long robotId) {
        return tourContentMapper.selectByRobotId(robotId);
    }

    @Override
    public SysTourContent getContent(Long contentId) {
        return tourContentMapper.selectById(contentId);
    }

    @Override
    @Transactional
    public int saveContent(SysTourContent content) {
        content.setUpdateTime(DateUtils.getNowDate());
        if (content.getContentId() != null) {
            return tourContentMapper.update(content);
        } else {
            content.setCreateTime(DateUtils.getNowDate());
            content.setCreateBy(SecurityUtils.getUsername());
            return tourContentMapper.insert(content);
        }
    }

    @Override
    @Transactional
    public int deleteContent(Long contentId) {
        return tourContentMapper.deleteById(contentId);
    }

    @Override
    @Transactional
    public int batchDeleteContents(List<Long> contentIds) {
        int count = 0;
        for (Long id : contentIds) {
            count += tourContentMapper.deleteById(id);
        }
        return count;
    }

    @Override
    public List<SysTourRoute> getRouteList() {
        return tourRouteMapper.selectList(new SysTourRoute());
    }

    @Override
    public SysTourRoute getRoute(Long routeId) {
        SysTourRoute route = tourRouteMapper.selectById(routeId);
        if (route != null) {
            route.setRoutePoints(tourRouteMapper.selectRoutePoints(routeId));
        }
        return route;
    }

    @Override
    @Transactional
    public int saveRoute(SysTourRoute route) {
        route.setUpdateTime(DateUtils.getNowDate());
        if (route.getRouteId() != null) {
            // 先删除旧的点位关联
            tourRouteMapper.deleteRoutePoints(route.getRouteId());
            int result = tourRouteMapper.update(route);
            // 插入新的点位关联
            if (route.getRoutePoints() != null && !route.getRoutePoints().isEmpty()) {
                for (SysRoutePoint point : route.getRoutePoints()) {
                    point.setRouteId(route.getRouteId());
                    tourRouteMapper.insertRoutePoint(point);
                }
            }
            return result;
        } else {
            route.setCreateTime(DateUtils.getNowDate());
            route.setCreateBy(SecurityUtils.getUsername());
            int result = tourRouteMapper.insert(route);
            // 插入点位关联
            if (route.getRoutePoints() != null && !route.getRoutePoints().isEmpty()) {
                for (SysRoutePoint point : route.getRoutePoints()) {
                    point.setRouteId(route.getRouteId());
                    tourRouteMapper.insertRoutePoint(point);
                }
            }
            return result;
        }
    }

    @Override
    @Transactional
    public int deleteRoute(Long routeId) {
        tourRouteMapper.deleteRoutePoints(routeId);
        return tourRouteMapper.deleteById(routeId);
    }

    @Override
    @Transactional
    public int deleteRouteById(Long routeId) {
        return deleteRoute(routeId);
    }

    @Override
    public List<SysRoutePoint> getRoutePoints(Long routeId) {
        return tourRouteMapper.selectRoutePoints(routeId);
    }

    @Override
    @Transactional
    public int saveRoutePoints(Long routeId, List<SysRoutePoint> points) {
        tourRouteMapper.deleteRoutePoints(routeId);
        int count = 0;
        for (SysRoutePoint point : points) {
            point.setRouteId(routeId);
            count += tourRouteMapper.insertRoutePoint(point);
        }
        return count;
    }

    @Override
    public String exportRoutes() {
        try {
            List<SysTourRoute> routes = tourRouteMapper.selectList(new SysTourRoute());
            return objectMapper.writeValueAsString(routes);
        } catch (Exception e) {
            e.printStackTrace();
            return "[]";
        }
    }

    @Override
    @Transactional
    public int importRoutes(String jsonData) {
        try {
            List<SysTourRoute> routes = objectMapper.readValue(jsonData, new TypeReference<List<SysTourRoute>>(){});
            int count = 0;
            for (SysTourRoute route : routes) {
                route.setCreateTime(DateUtils.getNowDate());
                route.setCreateBy(SecurityUtils.getUsername());
                count += tourRouteMapper.insert(route);
            }
            return count;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}