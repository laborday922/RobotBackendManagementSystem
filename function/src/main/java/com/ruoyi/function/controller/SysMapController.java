package com.ruoyi.function.controller;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.function.domain.SysMap;
import com.ruoyi.function.domain.SysPoint;
import com.ruoyi.function.service.ISysMapService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Api(tags = "地图管理")
@RestController
@RequestMapping("/func/map")
public class SysMapController extends BaseController {

    @Autowired
    private ISysMapService sysMapService;

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
        return success(sysMapService.selectById(mapId));
    }

    @ApiOperation("新增地图")
    @PostMapping
    public AjaxResult add(@RequestBody SysMap map) {
        return toAjax(sysMapService.insert(map));
    }

    @ApiOperation("修改地图")
    @PutMapping
    public AjaxResult edit(@RequestBody SysMap map) {
        return toAjax(sysMapService.update(map));
    }

    @ApiOperation("删除地图")
    @DeleteMapping("/{mapId}")
    public AjaxResult remove(@PathVariable Long mapId) {
        return toAjax(sysMapService.deleteById(mapId));
    }

    @ApiOperation("上传地图文件")
    @PostMapping("/upload")
    public AjaxResult upload(@RequestParam("file") MultipartFile file) {
        // 实现文件上传逻辑
        // 1. 保存文件到服务器
        // 2. 返回文件访问路径
        return success();
    }

    @ApiOperation("获取地图点位列表")
    @GetMapping("/points/{mapId}")
    public AjaxResult getPointsByMapId(@PathVariable Long mapId) {
        List<SysPoint> points = sysMapService.selectPointsByMapId(mapId);
        return success(points);
    }
}
