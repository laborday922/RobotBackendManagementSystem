package com.ruoyi.data.clean.controller;

import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.data.clean.service.ICleanRuleExecuteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/clean/execute")
public class CleanRuleExecuteController {

    @Autowired
    private ICleanRuleExecuteService executeService;

    /**
     * 立即执行清洗任务
     */
    @PostMapping("/clean/{id}")
    public AjaxResult execute(@PathVariable("id") Long id) {
        try {
            executeService.executeTask(id);
            return AjaxResult.success("执行成功");
        } catch (Exception e) {
            return AjaxResult.error("执行失败：" + e.getMessage());
        }
    }
}