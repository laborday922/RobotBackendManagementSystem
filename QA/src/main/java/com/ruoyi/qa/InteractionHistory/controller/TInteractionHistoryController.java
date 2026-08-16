package com.ruoyi.qa.InteractionHistory.controller;

import java.util.Date;

import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.qa.InteractionHistory.service.ITInteractionHistoryService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 交互历史记录Controller（QA 问答评价）
 *
 * @author xiaocai
 * @date 2026-03-13
 */
@RestController
@RequestMapping("/qa/interactionHistory")
public class TInteractionHistoryController
{
    @Autowired
    private ITInteractionHistoryService tInteractionHistoryService;

    /**
     * 提交问答评价（评分 + 评价内容 + 交互时间相关字段），与任务无关
     */
    @ApiOperation("提交问答评价")
    @Anonymous
    @PostMapping("/QAevaluate")
    public AjaxResult qaEvaluate(
            @RequestParam Long robotId,
            @RequestParam Long rating,
            @RequestParam(required = false) String evaluationText,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date interactionTime,
            @RequestParam(required = false) Long duration) {
        int rows = tInteractionHistoryService.saveQAEvaluation(robotId, rating, evaluationText, interactionTime, duration);
        return rows > 0 ? AjaxResult.success() : AjaxResult.error("提交评价失败");
    }
}
