package com.ruoyi.function.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.common.threadlocal.TenantContext;
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
import com.ruoyi.robots.websocket.RobotWebSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.ruoyi.common.utils.SecurityUtils.isAdmin;

@Service
public class SysTourServiceImpl implements ISysTourService {

    private static final Logger log = LoggerFactory.getLogger(SysTourServiceImpl.class);

    @Autowired
    private SysTourGeneralMapper tourGeneralMapper;

    @Autowired
    private SysTourContentMapper tourContentMapper;

    @Autowired
    private SysTourRouteMapper tourRouteMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired(required = false)
    private RobotWebSocketHandler webSocketHandler;

    // ========== 通用配置 ==========

    @Override
    public SysTourGeneral getGeneralConfig(Long robotId) {
        SysTourGeneral config = tourGeneralMapper.selectByRobotId(robotId);
        Long tenantId = TenantContext.get();
        if (config == null) {
            config = createDefaultGeneralConfig(robotId, tenantId);
            tourGeneralMapper.insert(config);
        } else if (!isAdmin(tenantId) && config.getTenantId() != null && !config.getTenantId().equals(tenantId)) {
            config = createDefaultGeneralConfig(robotId, tenantId);
        }
        return config;
    }

    private SysTourGeneral createDefaultGeneralConfig(Long robotId, Long tenantId) {
        SysTourGeneral config = new SysTourGeneral();
        config.setRobotId(robotId);
        config.setVoice("温柔女声");
        config.setVoiceInteraction("1");
        config.setStartCommand("开始讲解");
        config.setBeforeTip("即将开始讲解，请跟随我");
        config.setEndTip("本次讲解结束，谢谢");
        config.setAfterAction("stay");
        config.setTenantId(tenantId);
        return config;
    }

    @Override
    public int saveGeneralConfig(SysTourGeneral config) {
        config.setUpdateTime(DateUtils.getNowDate());
        config.setTenantId(TenantContext.get());

        SysTourGeneral existing = tourGeneralMapper.selectByRobotId(config.getRobotId());
        int result;
        if (existing != null) {
            config.setConfigId(existing.getConfigId());
            result = tourGeneralMapper.update(config);
        } else {
            config.setCreateTime(DateUtils.getNowDate());
            result = tourGeneralMapper.insert(config);
        }

        if (result > 0) {
            syncGeneralConfigToRobot(config);
        }

        return result;
    }

    private void syncGeneralConfigToRobot(SysTourGeneral config) {
        if (webSocketHandler == null) return;

        try {
            Long robotId = config.getRobotId();
            if (!webSocketHandler.isOnline(robotId)) return;

            Map<String, Object> requestData = new HashMap<>();
            requestData.put("action", "sync_tour_config");
            requestData.put("configType", "general");
            requestData.put("voice", config.getVoice());
            requestData.put("voiceInteraction", config.getVoiceInteraction());
            requestData.put("startCommand", config.getStartCommand());
            requestData.put("beforeTip", config.getBeforeTip());
            requestData.put("endTip", config.getEndTip());
            requestData.put("afterAction", config.getAfterAction());

            String correlationId = UUID.randomUUID().toString();
            webSocketHandler.sendRequest(robotId, requestData, correlationId);
        } catch (IOException e) {
            log.error("同步通用配置到机器人失败: {}", e.getMessage());
        }
    }

    // ========== 讲解内容 ==========

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
        content.setTenantId(TenantContext.get());

        int result;
        if (content.getContentId() != null) {
            result = tourContentMapper.update(content);
        } else {
            content.setCreateTime(DateUtils.getNowDate());
            content.setCreateBy(SecurityUtils.getUsername());
            result = tourContentMapper.insert(content);
        }

        if (result > 0 && content.getRobotId() != null) {
            syncContentToRobot(content);
        }

        return result;
    }

    private void syncContentToRobot(SysTourContent content) {
        if (webSocketHandler == null) return;

        try {
            Long robotId = content.getRobotId();
            if (!webSocketHandler.isOnline(robotId)) return;

            Map<String, Object> requestData = new HashMap<>();
            requestData.put("action", "sync_tour_config");
            requestData.put("configType", "content");
            requestData.put("contentId", content.getContentId());
            requestData.put("pointName", content.getPointName());
            requestData.put("pointDesc", content.getPointDesc());
            requestData.put("broadcastType", content.getBroadcastType());
            requestData.put("broadcastText", content.getBroadcastText());
            requestData.put("audioFile", content.getAudioFile());
            requestData.put("voiceType", content.getVoiceType());
            requestData.put("speechRate", content.getSpeechRate());
            requestData.put("intervalTime", content.getIntervalTime());
            requestData.put("armAction", content.getArmAction());
            requestData.put("chassisAngle", content.getChassisAngle());
            requestData.put("orderNum", content.getOrderNum());

            String correlationId = UUID.randomUUID().toString();
            webSocketHandler.sendRequest(robotId, requestData, correlationId);
        } catch (IOException e) {
            log.error("同步讲解内容到机器人失败: {}", e.getMessage());
        }
    }

    @Override
    @Transactional
    public int deleteContent(Long contentId) {
        SysTourContent content = tourContentMapper.selectById(contentId);
        int result = tourContentMapper.deleteById(contentId);
        if (result > 0 && content != null && content.getRobotId() != null) {
            notifyRobotDeleteConfig(content.getRobotId(), "content", contentId);
        }
        return result;
    }

    @Override
    @Transactional
    public int batchDeleteContents(List<Long> contentIds) {
        int count = 0;
        for (Long id : contentIds) {
            count += deleteContent(id);
        }
        return count;
    }

    // ========== 讲解路线 ==========

    @Override
    public List<SysTourRoute> getRouteList() {
        SysTourRoute route = new SysTourRoute();
        Long tenantId = TenantContext.get();
        if (!isAdmin(tenantId)) {
            route.setTenantId(tenantId);
        }
        return tourRouteMapper.selectList(route);
    }

    /**
     * 新增：根据机器人ID获取路线列表
     */
    @Override
    public List<SysTourRoute> getRouteListByRobotId(Long robotId) {
        SysTourRoute route = new SysTourRoute();
        route.setRobotId(robotId);
        Long tenantId = TenantContext.get();
        if (!isAdmin(tenantId)) {
            route.setTenantId(tenantId);
        }
        return tourRouteMapper.selectList(route);
    }

    /**
     * 根据机器人ID获取路线详情列表（包含所有点位顺序和讲解内容）
     */
    @Override
    public List<SysTourRoute> getRouteDetailListByRobotId(Long robotId) {
        return tourRouteMapper.selectRouteDetailByRobotId(robotId);
    }

    @Override
    public SysTourRoute getRoute(Long routeId) {
        SysTourRoute route = tourRouteMapper.selectById(routeId);
        if (route != null) {
            route.setRoutePoints(tourRouteMapper.selectRoutePoints(routeId));
        }
        return route;
    }

    /**
     * 根据 routePoints 生成 pointIds JSON 字符串
     */
    private String generatePointIds(List<SysRoutePoint> routePoints) {
        if (routePoints == null || routePoints.isEmpty()) {
            return null;
        }

        List<Long> pointIdList = routePoints.stream()
                .map(SysRoutePoint::getPointId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (pointIdList.isEmpty()) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(pointIdList);
        } catch (JsonProcessingException e) {
            log.error("生成pointIds失败", e);
            return null;
        }
    }

    @Override
    @Transactional
    public int saveRoute(SysTourRoute route) {
        // 验证 robotId 不能为空
        if (route.getRobotId() == null) {
            log.error("保存路线失败：robotId 不能为空");
            throw new RuntimeException("机器人ID不能为空");
        }

        route.setUpdateTime(DateUtils.getNowDate());
        route.setTenantId(TenantContext.get());

        log.info("saveRoute - routeName: {}, robotId: {}, routePoints数量: {}",
                route.getRouteName(), route.getRobotId(),
                route.getRoutePoints() == null ? 0 : route.getRoutePoints().size());

        // 根据 routePoints 生成 pointIds JSON 字符串
        String pointIds = generatePointIds(route.getRoutePoints());
        log.info("saveRoute - 生成的 pointIds: {}", pointIds);
        route.setPointIds(pointIds);

        // 更新路线中的 pointCount
        if (route.getRoutePoints() != null) {
            route.setPointCount(route.getRoutePoints().size());
        } else {
            route.setPointCount(0);
        }

        int result;
        if (route.getRouteId() != null) {
            // 更新：先删除旧关联，再插入新关联
            tourRouteMapper.deleteRoutePoints(route.getRouteId());
            result = tourRouteMapper.update(route);
            if (route.getRoutePoints() != null && !route.getRoutePoints().isEmpty()) {
                for (SysRoutePoint point : route.getRoutePoints()) {
                    point.setRouteId(route.getRouteId());
                    tourRouteMapper.insertRoutePoint(point);
                }
            }
        } else {
            // 新增
            route.setCreateTime(DateUtils.getNowDate());
            route.setCreateBy(SecurityUtils.getUsername());
            result = tourRouteMapper.insert(route);
            log.info("saveRoute - insert 成功, 生成的 routeId: {}", route.getRouteId());
            if (route.getRoutePoints() != null && !route.getRoutePoints().isEmpty()) {
                Long newRouteId = route.getRouteId();
                for (SysRoutePoint point : route.getRoutePoints()) {
                    point.setRouteId(newRouteId);
                    tourRouteMapper.insertRoutePoint(point);
                }
            }
        }

        if (result > 0) {
            syncRouteToRobot(route);
        }

        return result;
    }

    private void syncRouteToRobot(SysTourRoute route) {
        if (webSocketHandler == null) return;

        try {
            Long robotId = route.getRobotId();
            if (robotId == null || !webSocketHandler.isOnline(robotId)) return;

            Map<String, Object> requestData = new HashMap<>();
            requestData.put("action", "sync_tour_config");
            requestData.put("configType", "route");
            requestData.put("routeId", route.getRouteId());
            requestData.put("routeName", route.getRouteName());
            requestData.put("mapId", route.getMapId());
            requestData.put("pointIds", route.getPointIds());
            requestData.put("routePoints", route.getRoutePoints());

            String correlationId = UUID.randomUUID().toString();
            webSocketHandler.sendRequest(robotId, requestData, correlationId);
        } catch (Exception e) {
            log.error("同步路线到机器人失败: {}", e.getMessage());
        }
    }

    private void notifyRobotDeleteConfig(Long robotId, String configType, Long configId) {
        if (webSocketHandler == null) return;

        try {
            if (!webSocketHandler.isOnline(robotId)) return;

            Map<String, Object> requestData = new HashMap<>();
            requestData.put("action", "delete_tour_config");
            requestData.put("configType", configType);
            requestData.put("configId", configId);

            String correlationId = UUID.randomUUID().toString();
            webSocketHandler.sendRequest(robotId, requestData, correlationId);
        } catch (IOException e) {
            log.error("通知机器人删除配置失败: {}", e.getMessage());
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

        // 同步更新路线的 pointIds 和 pointCount
        SysTourRoute route = tourRouteMapper.selectById(routeId);
        if (route != null) {
            String pointIds = generatePointIds(points);
            route.setPointIds(pointIds);
            route.setPointCount(points != null ? points.size() : 0);
            tourRouteMapper.update(route);
        }

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
            SysTourRoute route = new SysTourRoute();
            Long tenantId = TenantContext.get();
            if (!isAdmin(tenantId)) {
                route.setTenantId(tenantId);
            }
            List<SysTourRoute> routes = tourRouteMapper.selectList(route);
            // 为每个路线填充 routePoints
            for (SysTourRoute r : routes) {
                r.setRoutePoints(tourRouteMapper.selectRoutePoints(r.getRouteId()));
            }
            return objectMapper.writeValueAsString(routes);
        } catch (Exception e) {
            log.error("导出路线失败", e);
            return "[]";
        }
    }

    @Override
    @Transactional
    public int importRoutes(String jsonData) {
        try {
            List<SysTourRoute> routes = objectMapper.readValue(jsonData, new TypeReference<List<SysTourRoute>>(){});
            int count = 0;
            Long tenantId = TenantContext.get();
            for (SysTourRoute route : routes) {
                route.setCreateTime(DateUtils.getNowDate());
                route.setCreateBy(SecurityUtils.getUsername());
                route.setTenantId(tenantId);

                // 重新生成 pointIds
                String pointIds = generatePointIds(route.getRoutePoints());
                route.setPointIds(pointIds);

                if (route.getRoutePoints() != null) {
                    route.setPointCount(route.getRoutePoints().size());
                }

                count += tourRouteMapper.insert(route);

                // 插入路线点位关联
                if (route.getRoutePoints() != null && !route.getRoutePoints().isEmpty()) {
                    Long newRouteId = route.getRouteId();
                    for (SysRoutePoint point : route.getRoutePoints()) {
                        point.setRouteId(newRouteId);
                        tourRouteMapper.insertRoutePoint(point);
                    }
                }
            }
            return count;
        } catch (Exception e) {
            log.error("导入路线失败", e);
            return 0;
        }
    }
}