package com.ruoyi.function.controller;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.function.domain.SysTourGeneral;
import com.ruoyi.function.domain.SysTourContent;
import com.ruoyi.function.domain.SysTourRoute;
import com.ruoyi.function.service.ISysTourService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiImplicitParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "智能讲解")
@RestController
@RequestMapping("/func/tour")
public class SysTourController extends BaseController {

    @Autowired
    private ISysTourService tourService;

    // ========== 通用配置 ==========
    @ApiOperation("获取通用配置")
    @ApiImplicitParam(name = "robotId", value = "机器人ID", required = true)
    @GetMapping("/general/{robotId}")
    public AjaxResult getGeneralConfig(@PathVariable Long robotId) {
        return success(tourService.getGeneralConfig(robotId));
    }

    @ApiOperation("保存通用配置")
    @PostMapping("/general")
    public AjaxResult saveGeneralConfig(@RequestBody SysTourGeneral config) {
        return toAjax(tourService.saveGeneralConfig(config));
    }

    // ========== 讲解内容 ==========
    @ApiOperation("获取讲解内容列表")
    @ApiImplicitParam(name = "robotId", value = "机器人ID", required = true)
    @GetMapping("/content/list/{robotId}")
    public AjaxResult getContentList(@PathVariable Long robotId) {
        return success(tourService.getContentList(robotId));
    }

    @ApiOperation("获取讲解内容详情")
    @ApiImplicitParam(name = "contentId", value = "内容ID", required = true)
    @GetMapping("/content/{contentId}")
    public AjaxResult getContent(@PathVariable Long contentId) {
        return success(tourService.getContent(contentId));
    }

    @ApiOperation("保存讲解内容")
    @PostMapping("/content")
    public AjaxResult saveContent(@RequestBody SysTourContent content) {
        return toAjax(tourService.saveContent(content));
    }

    @ApiOperation("删除讲解内容")
    @ApiImplicitParam(name = "contentId", value = "内容ID", required = true)
    @DeleteMapping("/content/{contentId}")
    public AjaxResult deleteContent(@PathVariable Long contentId) {
        return toAjax(tourService.deleteContent(contentId));
    }

    @ApiOperation("批量删除讲解内容")
    @DeleteMapping("/content/batch")
    public AjaxResult batchDeleteContents(@RequestBody List<Long> contentIds) {
        return toAjax(tourService.batchDeleteContents(contentIds));
    }

    // ========== 讲解路线 ==========
    @ApiOperation("获取路线列表")
    @GetMapping("/route/list")
    public AjaxResult getRouteList() {
        return success(tourService.getRouteList());
    }

    @ApiOperation("获取路线详情")
    @ApiImplicitParam(name = "routeId", value = "路线ID", required = true)
    @GetMapping("/route/{routeId}")
    public AjaxResult getRoute(@PathVariable Long routeId) {
        return success(tourService.getRoute(routeId));
    }

    @ApiOperation("保存路线")
    @PostMapping("/route")
    public AjaxResult saveRoute(@RequestBody SysTourRoute route) {
        return toAjax(tourService.saveRoute(route));
    }

    @ApiOperation("删除路线")
    @ApiImplicitParam(name = "routeId", value = "路线ID", required = true)
    @DeleteMapping("/route/{routeId}")
    public AjaxResult deleteRoute(@PathVariable Long routeId) {
        return toAjax(tourService.deleteRouteById(routeId));
    }

    @ApiOperation("导出路线")
    @GetMapping("/route/export")
    public AjaxResult exportRoutes() {
        return success(tourService.exportRoutes());
    }

    @ApiOperation("导入路线")
    @PostMapping("/route/import")
    public AjaxResult importRoutes(@RequestBody String jsonData) {
        return toAjax(tourService.importRoutes(jsonData));
    }
}