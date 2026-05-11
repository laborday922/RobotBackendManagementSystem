package com.ruoyi.mode.controller;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.mode.event.ModeSwitchCompletedEvent;
import com.ruoyi.mode.invoker.dto.ModeSwitchResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 模式切换回调接口
 * 用于接收机器人的异步回调
 */
@Api(tags = "模式切换回调")
@Slf4j
@RestController
@RequestMapping("/callback/mode")
public class ModeSwitchCallbackController extends BaseController {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @ApiOperation("模式切换结果回调")
    @PostMapping("/switchResult")
    public AjaxResult onSwitchResult(@RequestBody Map<String, Object> callbackData) {
        log.info("收到模式切换回调: {}", callbackData);

        String traceId = (String) callbackData.get("traceId");
        Boolean success = (Boolean) callbackData.getOrDefault("success", false);
        Object data = callbackData.get("data");
        String errorMsg = (String) callbackData.get("errorMsg");
        Long robotId = callbackData.get("robotId") != null ?
                Long.valueOf(callbackData.get("robotId").toString()) : null;

        if (traceId != null) {
            eventPublisher.publishEvent(new ModeSwitchCompletedEvent(this, robotId, traceId, success, data, errorMsg));
        }

        return success("收到回调");
    }
}