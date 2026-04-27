package com.ruoyi.taskmgt.controller;

import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.taskmgt.service.ITaskParamsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/taskmgt")
@RequiredArgsConstructor
public class TaskParamsController {
    private final ITaskParamsService taskParamsService;
    /**
     * 获取 API 的动态参数及选项
     */
    @GetMapping("/{id}/dynamic-params")
    public AjaxResult getDynamicParams(@PathVariable Long id, @RequestParam(required = false) Long robotId) {
        return AjaxResult.success(taskParamsService.getDynamicParams(id, robotId));}
}
