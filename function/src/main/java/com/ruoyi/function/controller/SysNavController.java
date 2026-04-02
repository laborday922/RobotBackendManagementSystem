package com.ruoyi.function.controller;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.function.domain.SysNavConfig;
import com.ruoyi.function.service.ISysNavConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

//import static jdk.vm.ci.hotspot.HotSpotCompilationRequestResult.success;

@Api(tags = "导航指引")
@RestController
@RequestMapping("/func/nav")
public class SysNavController extends BaseController {

    @Autowired
    private ISysNavConfigService navConfigService;

    @ApiOperation("获取导航配置")
    @GetMapping("/config")
    public AjaxResult getConfig() {
        return success(navConfigService.getCurrentConfig());
    }

    @ApiOperation("保存导航配置")
    @PostMapping("/config")
    public AjaxResult saveConfig(@RequestBody SysNavConfig config) {
        return toAjax(navConfigService.saveConfig(config));
    }

    @ApiOperation("上传地图")
    @PostMapping("/map")
    public AjaxResult uploadMap() {
        // 文件上传逻辑
        return success();
    }

    @ApiOperation("开始导航")
    @ApiImplicitParam(name = "pointName", value = "目标点位名称")
    @PostMapping("/start/{pointName}")
    public AjaxResult startNavigation(@PathVariable String pointName) {
        return toAjax(navConfigService.startNavigation(pointName));
    }

    @ApiOperation("紧急停止")
    @PostMapping("/stop")
    public AjaxResult emergencyStop() {
        return toAjax(navConfigService.emergencyStop());
    }
}
