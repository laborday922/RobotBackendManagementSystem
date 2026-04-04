package com.ruoyi.function.controller;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.function.domain.SysReceptionConfig;
import com.ruoyi.function.service.ISysReceptionConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiImplicitParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Api(tags = "业务接待配置")
@RestController
@RequestMapping("/func/reception")
public class SysReceptionController extends BaseController {

    @Autowired
    private ISysReceptionConfigService receptionConfigService;

    @ApiOperation("获取配置")
    @ApiImplicitParam(name = "robotId", value = "机器人ID", required = true)
    @GetMapping("/config/{robotId}")
    public AjaxResult getConfig(@PathVariable Long robotId) {
        return success(receptionConfigService.getConfigByRobotId(robotId));
    }

    @ApiOperation("保存配置")
    @PostMapping("/config")
    public AjaxResult saveConfig(@RequestBody SysReceptionConfig config) {
        return toAjax(receptionConfigService.saveConfig(config));
    }

    @ApiOperation("重置为默认配置")
    @ApiImplicitParam(name = "robotId", value = "机器人ID", required = true)
    @PutMapping("/reset/{robotId}")
    public AjaxResult resetConfig(@PathVariable Long robotId) {
        return toAjax(receptionConfigService.resetToDefault(robotId));
    }
}