package com.ruoyi.function.controller;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.function.constants.FunctionConstants;
import com.ruoyi.function.domain.SysMap;
import com.ruoyi.function.domain.SysPoint;
import com.ruoyi.function.controller.dto.request.MapUploadRequest;
import com.ruoyi.function.controller.dto.response.MapDetailResponse;
import com.ruoyi.function.enums.MapStatusEnum;
import com.ruoyi.function.exception.FunctionException;
import com.ruoyi.function.service.ISysMapService;
import com.ruoyi.function.util.MapFileUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.*;
import java.util.List;

@Api(tags = "地图管理")
@RestController
@RequestMapping("/func/map")
public class SysMapController extends BaseController {

    @Autowired
    private ISysMapService sysMapService;

    @Autowired
    private HttpServletRequest request;

    @ApiOperation("获取地图列表")
    @GetMapping("/list")
    public TableDataInfo list(SysMap map) {
        startPage();
        List<SysMap> list = sysMapService.selectList(map);
        return getDataTable(list);
    }

    @ApiOperation("获取地图详情")
    @GetMapping("/{mapId}")
    public AjaxResult getById(@PathVariable Long mapId) {
        SysMap map = sysMapService.selectById(mapId);
        if (map == null) {
            throw FunctionException.mapNotFound(mapId);
        }

        // 转换为响应DTO
        MapDetailResponse response = new MapDetailResponse();
        BeanUtils.copyProperties(map, response);
        response.setMapUrl(getMapImageUrl(map.getMapId()));
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
        // 验证状态值
        if (StringUtils.isNotEmpty(map.getStatus()) && !MapStatusEnum.isValid(map.getStatus())) {
            return error("状态值无效");
        }
        return toAjax(sysMapService.insert(map));
    }

    @ApiOperation("修改地图")
    @PutMapping
    public AjaxResult edit(@Valid @RequestBody SysMap map) {
        if (map.getMapId() == null) {
            return error("地图ID不能为空");
        }
        // 验证状态值
        if (StringUtils.isNotEmpty(map.getStatus()) && !MapStatusEnum.isValid(map.getStatus())) {
            return error("状态值无效");
        }
        return toAjax(sysMapService.update(map));
    }

    @ApiOperation("删除地图")
    @DeleteMapping("/{mapId}")
    public AjaxResult remove(@PathVariable Long mapId) {
        // 删除前检查是否存在
        SysMap map = sysMapService.selectById(mapId);
        if (map == null) {
            throw FunctionException.mapNotFound(mapId);
        }
        return toAjax(sysMapService.deleteById(mapId));
    }

    @ApiOperation("上传地图文件")
    @PostMapping("/upload")
    public AjaxResult upload(@RequestParam("file") MultipartFile file,
                             @RequestParam(value = "mapId", required = false) Long mapId,
                             @RequestParam(value = "mapName", required = false) String mapName) {
        try {
            // 使用工具类保存文件
            String fileName = MapFileUtil.saveFile(file, FunctionConstants.MAP_UPLOAD_PATH);

            SysMap resultMap = null;

            if (mapId != null && mapId > 0) {
                // 更新现有地图的文件
                SysMap existingMap = sysMapService.selectById(mapId);
                if (existingMap != null) {
                    // 删除旧文件
                    MapFileUtil.deleteFile(FunctionConstants.MAP_UPLOAD_PATH, existingMap.getMapFile());

                    existingMap.setMapFile(fileName);
                    sysMapService.update(existingMap);
                    resultMap = existingMap;
                } else {
                    throw FunctionException.mapNotFound(mapId);
                }
            }

            if (resultMap == null) {
                // 创建新地图记录
                resultMap = new SysMap();
                resultMap.setMapName(StringUtils.isNotEmpty(mapName) ? mapName : FunctionConstants.DEFAULT_MAP_NAME_PREFIX + System.currentTimeMillis());
                resultMap.setMapFile(fileName);
                resultMap.setStatus(MapStatusEnum.ENABLED.getCode());
                sysMapService.insert(resultMap);
            }

            // 返回包含完整URL的结果
            MapDetailResponse response = new MapDetailResponse();
            BeanUtils.copyProperties(resultMap, response);
            response.setMapUrl(getMapImageUrl(resultMap.getMapId()));

            return success(response);
        } catch (IOException e) {
            throw FunctionException.fileUploadFailed(file.getOriginalFilename());
        }
    }

    @ApiOperation("获取地图图片")
    @GetMapping("/image/{mapId}")
    public void getMapImage(@PathVariable Long mapId, HttpServletResponse response) {
        try {
            // 查询地图信息
            SysMap map = sysMapService.selectById(mapId);
            if (map == null || StringUtils.isEmpty(map.getMapFile())) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            // 构建文件完整路径
            String fullPath = MapFileUtil.getFullPath(FunctionConstants.MAP_UPLOAD_PATH, map.getMapFile());
            File file = new File(fullPath);
            if (!file.exists()) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            // 设置Content-Type
            String contentType = MapFileUtil.getContentType(map.getMapFile());
            response.setContentType(contentType);
            response.setHeader("Cache-Control", "max-age=3600"); // 缓存1小时

            // 读取文件并输出
            try (FileInputStream inputStream = new FileInputStream(file);
                 OutputStream outputStream = response.getOutputStream()) {
                byte[] buffer = new byte[FunctionConstants.BUFFER_SIZE];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                outputStream.flush();
            }

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation("获取地图点位列表")
    @GetMapping("/points/{mapId}")
    public AjaxResult getPointsByMapId(@PathVariable Long mapId) {
        List<SysPoint> points = sysMapService.selectPointsByMapId(mapId);
        return success(points);
    }

    /**
     * 获取地图图片访问URL
     */
    private String getMapImageUrl(Long mapId) {
        String baseUrl = getBaseUrl();
        return baseUrl + "/func/map/image/" + mapId;
    }

    /**
     * 获取基础URL
     */
    private String getBaseUrl() {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        String contextPath = request.getContextPath();

        if (serverPort == 80 || serverPort == 443) {
            return scheme + "://" + serverName + contextPath;
        }
        return scheme + "://" + serverName + ":" + serverPort + contextPath;
    }
}