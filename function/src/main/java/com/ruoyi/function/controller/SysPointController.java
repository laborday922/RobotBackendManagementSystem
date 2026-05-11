package com.ruoyi.function.controller;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.function.constants.MapConstants;
import com.ruoyi.function.controller.dto.request.PointCreateRequest;
import com.ruoyi.function.controller.dto.request.PointUpdateRequest;
import com.ruoyi.function.controller.dto.response.PointResponse;
import com.ruoyi.function.domain.SysMap;
import com.ruoyi.function.domain.SysPoint;
import com.ruoyi.function.enums.MapStatusEnum;
import com.ruoyi.function.enums.PointTypeEnum;
import com.ruoyi.function.exception.FunctionException;
import com.ruoyi.function.service.ISysMapService;
import com.ruoyi.function.service.ISysPointService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiImplicitParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Api(tags = "点位管理")
@RestController
@RequestMapping("/func/point")
public class SysPointController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(SysPointController.class);

    @Autowired
    private ISysPointService sysPointService;

    @Autowired
    private ISysMapService sysMapService;

    @Autowired(required = false)
    private com.ruoyi.taskmgt.websocket.TaskRobotWebSocketHandler taskRobotWebSocketHandler;

    // ========== 具体路径接口放在前面，避免与路径变量冲突 ==========

    @ApiOperation("获取点位列表")
    @GetMapping("/list")
    public TableDataInfo list(SysPoint point) {
        startPage();
        List<SysPoint> list = sysPointService.selectList(point);
        return getDataTable(list);
    }

    /**
     * 根据机器人ID直接获取点位列表（不通过地图）
     */
    @ApiOperation("根据机器人ID直接获取点位列表")
    @ApiImplicitParam(name = "robotId", value = "机器人ID", required = true)
    @GetMapping("/list/byRobot")
    public AjaxResult getPointsByRobotId(@RequestParam Long robotId) {
        try {
            List<SysPoint> points = sysPointService.selectByRobotId(robotId);
            if (points == null) {
                points = new ArrayList<>();
            }
            log.info("根据机器人ID获取点位成功, robotId={}, 点位数量={}", robotId, points.size());
            return success(points);
        } catch (Exception e) {
            log.error("根据机器人ID获取点位失败", e);
            return error("获取点位失败：" + e.getMessage());
        }
    }

    /**
     * 从机器人获取点位位置列表
     */
    @ApiOperation("从机器人获取点位位置列表")
    @GetMapping("/syncPositionsFromRobot")
    public AjaxResult syncPositionsFromRobot(@RequestParam Long robotId) {
        try {
            log.info("开始从机器人获取点位位置, robotId={}", robotId);

            if (taskRobotWebSocketHandler == null) {
                log.warn("TaskRobotWebSocketHandler 未注入，返回模拟数据");
                return success(getMockPositions());
            }

            if (!taskRobotWebSocketHandler.isOnline(robotId)) {
                return error("机器人不在线，无法获取点位位置数据");
            }

            Map<String, Object> request = new HashMap<>();
            request.put("action", "getPoints");
            request.put("operationId", 3);

            String correlationId = UUID.randomUUID().toString();

            CompletableFuture<com.ruoyi.common.core.websocket.RobotWebSocketMessage> future =
                    taskRobotWebSocketHandler.sendAndWaitRaw(robotId, request, correlationId, 10);

            com.ruoyi.common.core.websocket.RobotWebSocketMessage response = future.get(10, TimeUnit.SECONDS);

            if (response.getData() != null) {
                Map<String, Object> respData = (Map<String, Object>) response.getData();
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> points = (List<Map<String, Object>>) respData.get("points");
                if (points != null && !points.isEmpty()) {
                    log.info("成功获取机器人点位位置, 数量={}", points.size());
                    return success(points);
                }
            }

            return error("机器人未返回点位数据");
        } catch (Exception e) {
            log.error("获取机器人点位位置失败", e);
            return success(getMockPositions());
        }
    }

    /**
     * 获取模拟点位位置数据（用于测试）
     */
    private List<Map<String, Object>> getMockPositions() {
        List<Map<String, Object>> mockPoints = new ArrayList<>();

        Map<String, Object> point1 = new HashMap<>();
        point1.put("id", "1");
        point1.put("name", "充电桩");
        point1.put("code", "CHARGE");
        point1.put("x", 100.5);
        point1.put("y", 200.3);
        point1.put("type", "normal");
        mockPoints.add(point1);

        Map<String, Object> point2 = new HashMap<>();
        point2.put("id", "2");
        point2.put("name", "仓库A");
        point2.put("code", "WAREHOUSE_A");
        point2.put("x", 150.0);
        point2.put("y", 250.0);
        point2.put("type", "normal");
        mockPoints.add(point2);

        return mockPoints;
    }

    @ApiOperation("获取点位播报配置")
    @GetMapping("/voice/{pointId}")
    public AjaxResult getPointVoiceConfig(@PathVariable Long pointId) {
        SysPoint point = sysPointService.selectById(pointId);
        if (point == null) {
            return error("点位不存在");
        }

        Map<String, Object> config = new HashMap<>();
        config.put("voiceType", point.getVoiceType() != null ? point.getVoiceType() : "default");
        config.put("beforeMsg", point.getBeforeMsg());
        config.put("duringMsg", point.getDuringMsg());
        config.put("afterMsg", point.getAfterMsg());

        return success(config);
    }

    @ApiOperation("保存点位播报配置")
    @PostMapping("/voice/save")
    public AjaxResult savePointVoiceConfig(@RequestBody Map<String, Object> params) {
        Long pointId = Long.valueOf(params.get("pointId").toString());
        String voiceType = (String) params.get("voiceType");
        String beforeMsg = (String) params.get("beforeMsg");
        String duringMsg = (String) params.get("duringMsg");
        String afterMsg = (String) params.get("afterMsg");

        SysPoint point = sysPointService.selectById(pointId);
        if (point == null) {
            return error("点位不存在");
        }

        point.setVoiceType(voiceType);
        point.setBeforeMsg(beforeMsg);
        point.setDuringMsg(duringMsg);
        point.setAfterMsg(afterMsg);

        return toAjax(sysPointService.update(point));
    }

    @ApiOperation("根据机器人ID获取点位播报配置列表")
    @GetMapping("/voice/listByRobot")
    public AjaxResult getPointVoiceListByRobot(@RequestParam Long robotId) {
        try {
            List<SysPoint> points = sysPointService.selectByRobotId(robotId);

            if (points == null || points.isEmpty()) {
                return success(new ArrayList<>());
            }

            List<Map<String, Object>> result = points.stream().map(point -> {
                Map<String, Object> config = new HashMap<>();
                config.put("pointId", point.getPointId());
                config.put("pointName", point.getPointName());
                config.put("mapId", point.getMapId());
                config.put("voiceType", point.getVoiceType() != null ? point.getVoiceType() : "default");
                config.put("beforeMsg", point.getBeforeMsg());
                config.put("duringMsg", point.getDuringMsg());
                config.put("afterMsg", point.getAfterMsg());
                return config;
            }).collect(Collectors.toList());

            return success(result);
        } catch (Exception e) {
            log.error("获取点位播报配置列表失败", e);
            return error("获取失败：" + e.getMessage());
        }
    }

    @ApiOperation("根据地图ID获取点位播报配置列表")
    @GetMapping("/voice/listByMap")
    public AjaxResult getPointVoiceListByMap(@RequestParam Long mapId) {
        try {
            List<SysPoint> points = sysPointService.selectByMapId(mapId);

            if (points == null || points.isEmpty()) {
                return success(new ArrayList<>());
            }

            List<Map<String, Object>> result = points.stream().map(point -> {
                Map<String, Object> config = new HashMap<>();
                config.put("pointId", point.getPointId());
                config.put("pointName", point.getPointName());
                config.put("voiceType", point.getVoiceType() != null ? point.getVoiceType() : "default");
                config.put("beforeMsg", point.getBeforeMsg());
                config.put("duringMsg", point.getDuringMsg());
                config.put("afterMsg", point.getAfterMsg());
                return config;
            }).collect(Collectors.toList());

            return success(result);
        } catch (Exception e) {
            log.error("获取地图点位播报配置失败", e);
            return error("获取失败：" + e.getMessage());
        }
    }

    @ApiOperation("获取默认地图的点位列表")
    @GetMapping("/defaultPoints")
    public AjaxResult getDefaultPoints() {
        try {
            ensureDefaultMapExists();

            List<SysPoint> points = sysPointService.selectByMapId(MapConstants.DEFAULT_MAP_ID);
            if (points == null) {
                points = new ArrayList<>();
            }
            log.info("获取默认地图点位成功, 数量: {}", points.size());
            return success(points);
        } catch (Exception e) {
            log.error("获取默认地图点位失败", e);
            return error("获取默认地图点位失败：" + e.getMessage());
        }
    }

    // ========== 路径变量接口放在最后 ==========

    @ApiOperation("根据ID获取点位详情")
    @GetMapping("/{pointId}")
    public AjaxResult getById(@PathVariable Long pointId) {
        SysPoint point = sysPointService.selectById(pointId);
        if (point == null) {
            throw FunctionException.pointNotFound(pointId);
        }

        PointResponse response = new PointResponse();
        BeanUtils.copyProperties(point, response);

        PointTypeEnum typeEnum = PointTypeEnum.getByCode(point.getPointType());
        response.setPointTypeText(typeEnum.getInfo());

        if (MapStatusEnum.ENABLED.getCode().equals(point.getStatus())) {
            response.setStatusText(MapStatusEnum.ENABLED.getInfo());
        } else {
            response.setStatusText(MapStatusEnum.DISABLED.getInfo());
        }

        return success(response);
    }

    // ========== 私有方法 ==========

    /**
     * 获取有效的地图ID（如果为空或<=0则使用默认地图）
     */
    private Long getEffectiveMapId(Long requestMapId) {
        Long mapId = requestMapId;
        if (mapId == null || mapId <= 0) {
            mapId = MapConstants.DEFAULT_MAP_ID;
            log.debug("地图ID为空或无效，使用默认地图ID: {}", mapId);
        }
        return mapId;
    }

    /**
     * 确保默认地图存在
     */
    private void ensureDefaultMapExists() {
        try {
            SysMap defaultMap = sysMapService.selectById(MapConstants.DEFAULT_MAP_ID);
            if (defaultMap == null) {
                defaultMap = new SysMap();
                defaultMap.setMapId(MapConstants.DEFAULT_MAP_ID);
                defaultMap.setMapName(MapConstants.DEFAULT_MAP_NAME);
                defaultMap.setRobotId(MapConstants.DEFAULT_ROBOT_ID);
                defaultMap.setStatus(MapStatusEnum.ENABLED.getCode());
                defaultMap.setIsEnable(1);
                defaultMap.setDelFlag("0");
                defaultMap.setPointCount(0);
                defaultMap.setVersion("1.0");
                sysMapService.insert(defaultMap);
                log.info("自动创建默认地图成功，ID: {}", MapConstants.DEFAULT_MAP_ID);
            }
        } catch (Exception e) {
            log.error("创建/检查默认地图失败", e);
        }
    }

    // ========== CRUD 操作接口 ==========

    @ApiOperation("新增点位")
    @PostMapping
    public AjaxResult add(@Valid @RequestBody PointCreateRequest request) {
        ensureDefaultMapExists();

        Long mapId = getEffectiveMapId(request.getMapId());

        SysPoint point = new SysPoint();

        // 关键修改：如果前端传了 pointId，直接使用；否则自动生成或者报错
        if (request.getPointId() != null) {
            // 检查该 pointId 是否已存在
            SysPoint existing = sysPointService.selectById(request.getPointId());
            if (existing != null) {
                return error("点位ID " + request.getPointId() + " 已存在，无法创建");
            }
            point.setPointId(request.getPointId());
        } else if (request.getRobotPositionId() != null) {
            // 兼容使用 robotPositionId 作为 pointId
            SysPoint existing = sysPointService.selectById(request.getRobotPositionId());
            if (existing != null) {
                return error("点位ID " + request.getRobotPositionId() + " 已存在，无法创建");
            }
            point.setPointId(request.getRobotPositionId());
        } else {
            return error("请选择机器人点位位置");
        }

        point.setMapId(mapId);
        point.setRobotId(request.getRobotId());
        point.setPointName(request.getPointName());
        point.setPointCode(request.getPointCode());
        point.setPointType(request.getPointType());
        point.setStatus(request.getStatus());
        point.setOrderNum(request.getOrderNum());
        point.setRemark(request.getRemark());

        if (point.getVoiceType() == null) {
            point.setVoiceType("default");
        }

        log.info("新增点位: pointId={}, pointName={}, mapId={}, robotId={}",
                point.getPointId(), point.getPointName(), mapId, request.getRobotId());

        return toAjax(sysPointService.insert(point));
    }

    @ApiOperation("修改点位")
    @PutMapping
    public AjaxResult edit(@Valid @RequestBody PointUpdateRequest request) {
        SysPoint existing = sysPointService.selectById(request.getPointId());
        if (existing == null) {
            throw FunctionException.pointNotFound(request.getPointId());
        }

        Long mapId = getEffectiveMapId(request.getMapId());

        SysPoint point = new SysPoint();
        point.setPointId(request.getPointId());
        point.setMapId(mapId);

        if (request.getRobotId() != null) {
            point.setRobotId(request.getRobotId());
        }
        if (request.getPointName() != null) {
            point.setPointName(request.getPointName());
        }
        if (request.getPointCode() != null) {
            point.setPointCode(request.getPointCode());
        }
        if (request.getPointType() != null) {
            point.setPointType(request.getPointType());
        }
        if (request.getStatus() != null) {
            point.setStatus(request.getStatus());
        }
        if (request.getOrderNum() != null) {
            point.setOrderNum(request.getOrderNum());
        }
        if (request.getRemark() != null) {
            point.setRemark(request.getRemark());
        }

        log.info("更新点位: pointId={}, pointName={}, mapId={}, robotId={}",
                point.getPointId(), point.getPointName(), mapId, request.getRobotId());

        return toAjax(sysPointService.update(point));
    }

    @ApiOperation("删除点位")
    @DeleteMapping("/{pointId}")
    public AjaxResult remove(@PathVariable Long pointId) {
        SysPoint point = sysPointService.selectById(pointId);
        if (point == null) {
            throw FunctionException.pointNotFound(pointId);
        }
        log.info("删除点位: pointId={}, pointName={}", pointId, point.getPointName());
        return toAjax(sysPointService.deleteById(pointId));
    }

    @ApiOperation("批量删除点位")
    @DeleteMapping("/batch")
    public AjaxResult batchRemove(@RequestBody Long[] pointIds) {
        if (pointIds == null || pointIds.length == 0) {
            return error("请选择要删除的点位");
        }
        log.info("批量删除点位: count={}", pointIds.length);
        return toAjax(sysPointService.deleteByIds(pointIds));
    }
}