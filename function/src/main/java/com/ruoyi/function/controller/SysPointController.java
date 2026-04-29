package com.ruoyi.function.controller;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
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
import io.swagger.annotations.ApiParam;
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

    @ApiOperation("获取点位列表")
    @GetMapping("/list")
    public TableDataInfo list(SysPoint point) {
        startPage();
        List<SysPoint> list = sysPointService.selectList(point);
        return getDataTable(list);
    }

    @ApiOperation("根据ID获取点位详情")
    @GetMapping("/{pointId}")
    public AjaxResult getById(@PathVariable Long pointId) {
        SysPoint point = sysPointService.selectById(pointId);
        if (point == null) {
            throw FunctionException.pointNotFound(pointId);
        }

        // 转换为响应DTO
        PointResponse response = new PointResponse();
        BeanUtils.copyProperties(point, response);

        // 补充类型文本
        PointTypeEnum typeEnum = PointTypeEnum.getByCode(point.getPointType());
        response.setPointTypeText(typeEnum.getInfo());

        // 补充状态文本
        if (MapStatusEnum.ENABLED.getCode().equals(point.getStatus())) {
            response.setStatusText(MapStatusEnum.ENABLED.getInfo());
        } else {
            response.setStatusText(MapStatusEnum.DISABLED.getInfo());
        }

        return success(response);
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

    /**
     * 根据机器人ID获取所有点位的播报配置
     * 用于机器人端同步配置
     */
    @ApiOperation("根据机器人ID获取点位播报配置列表")
    @GetMapping("/voice/listByRobot")
    public AjaxResult getPointVoiceListByRobot(@RequestParam Long robotId) {
        try {
            // 1. 根据机器人ID获取关联的地图
            SysMap queryMap = new SysMap();
            queryMap.setRobotId(String.valueOf(robotId));
            queryMap.setDelFlag("0");
            List<SysMap> maps = sysMapService.selectList(queryMap);

            if (maps == null || maps.isEmpty()) {
                return success(new ArrayList<>());
            }

            // 2. 获取所有地图的点位ID
            List<Long> mapIds = maps.stream()
                    .map(SysMap::getMapId)
                    .collect(Collectors.toList());

            // 3. 查询这些地图下的所有点位播报配置
            List<SysPoint> points = sysPointService.selectByMapIds(mapIds);

            if (points == null || points.isEmpty()) {
                return success(new ArrayList<>());
            }

            // 4. 只返回播报配置相关的字段
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

    /**
     * 根据地图ID获取点位播报配置（用于机器人导航时查询）
     */
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

    @ApiOperation("新增点位")
    @PostMapping
    public AjaxResult add(@Valid @RequestBody PointCreateRequest request) {
        SysPoint point = new SysPoint();
        BeanUtils.copyProperties(request, point);
        // 设置默认播报类型
        if (point.getVoiceType() == null) {
            point.setVoiceType("default");
        }
        return toAjax(sysPointService.insert(point));
    }

    @ApiOperation("修改点位")
    @PutMapping
    public AjaxResult edit(@Valid @RequestBody PointUpdateRequest request) {
        // 检查点位是否存在
        SysPoint existing = sysPointService.selectById(request.getPointId());
        if (existing == null) {
            throw FunctionException.pointNotFound(request.getPointId());
        }

        SysPoint point = new SysPoint();
        BeanUtils.copyProperties(request, point);
        return toAjax(sysPointService.update(point));
    }

    @ApiOperation("删除点位")
    @DeleteMapping("/{pointId}")
    public AjaxResult remove(@PathVariable Long pointId) {
        SysPoint point = sysPointService.selectById(pointId);
        if (point == null) {
            throw FunctionException.pointNotFound(pointId);
        }
        return toAjax(sysPointService.deleteById(pointId));
    }

    @ApiOperation("批量删除点位")
    @DeleteMapping("/batch")
    public AjaxResult batchRemove(@RequestBody Long[] pointIds) {
        if (pointIds == null || pointIds.length == 0) {
            return error("请选择要删除的点位");
        }
        return toAjax(sysPointService.deleteByIds(pointIds));
    }
}