package com.ruoyi.data.clean.controller;

import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.data.clean.domain.bo.CleanManualExecuteRequest;
import com.ruoyi.data.clean.service.ICleanRuleExecuteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/clean/execute")
public class CleanRuleExecuteController {

    @Autowired
    private ICleanRuleExecuteService executeService;

    /**
     * 手动立即执行清洗
     */
    @PostMapping("/manual")
    public AjaxResult executeManual(@RequestBody CleanManualExecuteRequest request) {
        try {
            executeService.executeManual(request.getConfigJson(), request.getApplyDataSource());
            return AjaxResult.success("执行成功");
        } catch (Exception e) {
            return AjaxResult.error("执行失败：" + e.getMessage());
        }
    }
}
