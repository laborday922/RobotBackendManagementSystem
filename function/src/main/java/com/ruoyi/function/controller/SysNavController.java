package com.ruoyi.function.controller;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.function.controller.dto.request.NavigationRequest;
import com.ruoyi.function.controller.dto.response.NavigationResponse;
import com.ruoyi.function.domain.SysNavConfig;
import com.ruoyi.function.enums.NavVoiceTypeEnum;
import com.ruoyi.function.service.ISysNavConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Api(tags = "导航指引")
@RestController
@RequestMapping("/func/nav")
public class SysNavController extends BaseController {

    @Autowired
    private ISysNavConfigService navConfigService;

    @ApiOperation("获取导航配置")
    @GetMapping("/config")
    public AjaxResult getConfig() {
        SysNavConfig config = navConfigService.getCurrentConfig();
        return success(config);
    }

    @ApiOperation("保存导航配置")
    @PostMapping("/config")
    public AjaxResult saveConfig(@Valid @RequestBody SysNavConfig config) {
        // 验证播报类型
        if (config.getVoiceType() != null) {
            boolean valid = false;
            for (NavVoiceTypeEnum type : NavVoiceTypeEnum.values()) {
                if (type.getCode().equals(config.getVoiceType())) {
                    valid = true;
                    break;
                }
            }
            if (!valid) {
                return error("无效的播报类型");
            }
        }
        return toAjax(navConfigService.saveConfig(config));
    }

    @ApiOperation("开始导航")
    @PostMapping("/start")
    public AjaxResult startNavigation(@Valid @RequestBody NavigationRequest request) {
        int result = navConfigService.startNavigation(request.getPointName());
        if (result > 0) {
            NavigationResponse response = NavigationResponse.success(request.getPointName());
            return success(response);
        }
        return error("导航启动失败");
    }

    @ApiOperation("紧急停止")
    @PostMapping("/stop")
    public AjaxResult emergencyStop() {
        int result = navConfigService.emergencyStop();
        if (result > 0) {
            return success("导航已停止");
        }
        return error("停止失败");
    }
}