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
import io.swagger.annotations.ApiParam;
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
    public AjaxResult getConfig(
            @ApiParam(value = "机器人ID", required = true)
            @RequestParam(required = false) String robotId) {

        if (robotId == null || robotId.isEmpty()) {
            return error("机器人ID不能为空");
        }

        SysNavConfig config = navConfigService.getConfigByRobotId(robotId);
        return success(config);
    }

    @ApiOperation("保存导航配置")
    @PostMapping("/config")
    public AjaxResult saveConfig(@Valid @RequestBody SysNavConfig config) {
        // 验证机器人ID
        if (config.getRobotId() == null || config.getRobotId().isEmpty()) {
            return error("机器人ID不能为空");
        }

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
}
