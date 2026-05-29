package com.ruoyi.function.controller;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.function.controller.dto.response.MapDetailResponse;
import com.ruoyi.function.domain.SysMap;
import com.ruoyi.function.domain.SysPoint;
import com.ruoyi.function.enums.MapStatusEnum;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api(tags = "地图管理")
@RestController
@RequestMapping("/func/map")
public class SysMapController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(SysMapController.class);

    @Autowired
    private ISysMapService sysMapService;

    @Autowired
    private ISysPointService sysPointService;

    @Autowired
    private HttpServletRequest request;

    @Value("${ruoyi.map.upload-path:./uploads/map/}")
    private String uploadPath;

    @ApiOperation("获取地图列表")
    @GetMapping("/list")
    public TableDataInfo list(
            @ApiParam(value = "地图名称") @RequestParam(required = false) String mapName,
            @ApiParam(value = "机器人ID", required = false) @RequestParam(required = false) String robotId,
            @ApiParam(value = "状态") @RequestParam(required = false) String status) {

        startPage();

        // 构建查询参数
        SysMap queryParam = new SysMap();
        queryParam.setMapName(mapName);
        queryParam.setRobotId(robotId);
        queryParam.setStatus(status);
        queryParam.setDelFlag("0"); // 只查询未删除的

        List<SysMap> list = sysMapService.selectList(queryParam);

        // 为每个地图设置Base64图片数据
        for (SysMap sysMap : list) {
            if (StringUtils.isNotEmpty(sysMap.getMapBase64())) {
                // 标记有图片数据，前端可以直接使用
                sysMap.setHasImage(true);
            }
        }
        return getDataTable(list);
    }

    @ApiOperation("获取地图详情")
    @GetMapping("/{mapId}")
    public AjaxResult getById(@PathVariable Long mapId) {
        SysMap map = sysMapService.selectById(mapId);
        if (map == null) {
            throw FunctionException.mapNotFound(mapId);
        }

        MapDetailResponse response = new MapDetailResponse();
        BeanUtils.copyProperties(map, response);

        // 直接返回Base64图片数据
        if (StringUtils.isNotEmpty(map.getMapBase64())) {
            response.setMapBase64(map.getMapBase64());
        }

        if (MapStatusEnum.ENABLED.getCode().equals(map.getStatus())) {
            response.setStatusText(MapStatusEnum.ENABLED.getInfo());
        } else {
            response.setStatusText(MapStatusEnum.DISABLED.getInfo());
        }

        return success(response);
    }

    @ApiOperation("新增地图")
    @PostMapping
    public AjaxResult add(@Valid @RequestBody SysMap map) {
        if (StringUtils.isEmpty(map.getRobotId())) {
            return error("机器人ID不能为空");
        }
        if (StringUtils.isNotEmpty(map.getStatus()) && !MapStatusEnum.isValid(map.getStatus())) {
            return error("状态值无效");
        }
        map.setDelFlag("0");
        return toAjax(sysMapService.insert(map));
    }

    @ApiOperation("修改地图")
    @PutMapping
    public AjaxResult edit(@Valid @RequestBody SysMap map) {
        if (map.getMapId() == null) {
            return error("地图ID不能为空");
        }
        if (StringUtils.isNotEmpty(map.getStatus()) && !MapStatusEnum.isValid(map.getStatus())) {
            return error("状态值无效");
        }
        return toAjax(sysMapService.update(map));
    }

    @ApiOperation("删除地图")
    @DeleteMapping("/{mapId}")
    public AjaxResult remove(@PathVariable Long mapId) {
        SysMap map = sysMapService.selectById(mapId);
        if (map == null) {
            throw FunctionException.mapNotFound(mapId);
        }
        return toAjax(sysMapService.deleteById(mapId));
    }

    @ApiOperation("上传地图文件")
    @PostMapping("/upload")
    public AjaxResult upload(
            @ApiParam(value = "地图文件", required = true) @RequestParam("file") MultipartFile file,
            @ApiParam(value = "地图ID（更新时传入）") @RequestParam(value = "mapId", required = false) Long mapId,
            @ApiParam(value = "地图名称（新增时传入）") @RequestParam(value = "mapName", required = false) String mapName,
            @ApiParam(value = "机器人ID（新增时传入）", required = false) @RequestParam(value = "robotId", required = false) String robotId) {

        try {
            log.info("开始上传地图文件: {}, 大小: {} bytes, mapId: {}, robotId: {}",
                    file.getOriginalFilename(), file.getSize(), mapId, robotId);

            // 验证文件类型
            String contentType = file.getContentType();
            if (contentType == null || (!contentType.startsWith("image/"))) {
                return error("请上传图片文件");
            }

            // 验证文件大小 (20MB)
            if (file.getSize() > 20 * 1024 * 1024) {
                return error("文件大小不能超过20MB");
            }

            // 将图片转换为Base64
            byte[] bytes = file.getBytes();
            String base64Image = Base64.getEncoder().encodeToString(bytes);

            // 获取图片类型
            String base64Data = "data:" + contentType + ";base64," + base64Image;

            log.info("Base64转换完成，长度: {}", base64Data.length());

            SysMap resultMap = null;

            if (mapId != null && mapId > 0) {
                // 更新现有地图
                SysMap existingMap = sysMapService.selectById(mapId);
                if (existingMap != null) {
                    existingMap.setMapBase64(base64Data);
                    existingMap.setMapFile(file.getOriginalFilename());
                    existingMap.setUpdateTime(getNowDate());
                    sysMapService.update(existingMap);
                    resultMap = existingMap;
                    log.info("更新地图，ID: {}", mapId);
                } else {
                    throw FunctionException.mapNotFound(mapId);
                }
            } else {
                // 新增地图，需要 robotId
                if (StringUtils.isEmpty(robotId)) {
                    return error("机器人ID不能为空");
                }

                resultMap = new SysMap();
                String defaultName = StringUtils.isNotEmpty(mapName) ? mapName :
                        file.getOriginalFilename().substring(0, file.getOriginalFilename().lastIndexOf("."));
                resultMap.setMapName(defaultName);
                resultMap.setRobotId(robotId);
                resultMap.setMapBase64(base64Data);
                resultMap.setMapFile(file.getOriginalFilename());
                resultMap.setStatus(MapStatusEnum.ENABLED.getCode());
                resultMap.setDelFlag("0");
                resultMap.setPointCount(0);
                resultMap.setVersion("1.0");
                resultMap.setIsEnable(1);
                resultMap.setCreateTime(getNowDate());
                sysMapService.insert(resultMap);
                log.info("新增地图，ID: {}，关联机器人: {}", resultMap.getMapId(), robotId);
            }

            // 设置返回数据
            Map<String, Object> response = new HashMap<>();
            response.put("mapId", resultMap.getMapId());
            response.put("mapName", resultMap.getMapName());
            response.put("robotId", resultMap.getRobotId());
            response.put("mapBase64", base64Data);
            response.put("status", resultMap.getStatus());

            log.info("地图上传成功，ID: {}", resultMap.getMapId());
            return success(response);

        } catch (IOException e) {
            log.error("文件上传失败", e);
            return error("文件上传失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("地图上传异常", e);
            return error("地图上传失败: " + e.getMessage());
        }
    }

    @ApiOperation("获取地图点位列表")
    @GetMapping("/points")
    public AjaxResult getPoints(@RequestParam(required = false) Long mapId, @RequestParam(required = false) Long robotId) {
        if (mapId == null) {
            return error("地图ID不能为空");
        }
        if (robotId == null) {
            return error("机器人ID不能为空");
        }

        SysPoint query = new SysPoint();
        query.setMapId(mapId);
        query.setRobotId(robotId);
        query.setDelFlag("0");

        List<SysPoint> points = sysPointService.selectList(query);
        return success(points);
    }

    @ApiOperation("获取地图Base64数据")
    @GetMapping("/base64/{mapId}")
    public AjaxResult getMapBase64(@PathVariable Long mapId) {
        SysMap map = sysMapService.selectById(mapId);
        if (map == null) {
            return error("地图不存在");
        }

        Map<String, String> result = new HashMap<>();
        result.put("base64", map.getMapBase64());
        result.put("mapName", map.getMapName());
        result.put("robotId", map.getRobotId());

        return success(result);
    }

    @ApiOperation("根据机器人ID获取地图列表")
    @GetMapping("/list/byRobot")
    public AjaxResult getListByRobotId(
            @ApiParam(value = "机器人ID", required = true) @RequestParam String robotId) {
        if (StringUtils.isEmpty(robotId)) {
            return error("机器人ID不能为空");
        }

        SysMap queryParam = new SysMap();
        queryParam.setRobotId(robotId);
        queryParam.setStatus(MapStatusEnum.ENABLED.getCode());
        queryParam.setDelFlag("0");

        List<SysMap> list = sysMapService.selectList(queryParam);

        // 为每个地图设置Base64图片数据
        for (SysMap sysMap : list) {
            if (StringUtils.isNotEmpty(sysMap.getMapBase64())) {
                sysMap.setHasImage(true);
            }
        }

        return success(list);
    }

    private java.util.Date getNowDate() {
        return new java.util.Date();
    }
}
