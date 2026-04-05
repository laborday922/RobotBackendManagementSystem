package com.ruoyi.data.ai.controller;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.data.ai.service.IssueClassificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai/issue-classification")
public class IssueClassificationController extends BaseController {

    @Autowired
    private IssueClassificationService service;

    @GetMapping("/distribution")
    public AjaxResult getDistribution(@RequestParam String time_range) {
        return success(service.getIssueDistribution(time_range));
    }

    @GetMapping("/trend")
    public AjaxResult getTrend(
            @RequestParam String category,
            @RequestParam String granularity,
            @RequestParam String time_range
    ) {
        return success(service.getIssueTrend(category, granularity, time_range));
    }
}