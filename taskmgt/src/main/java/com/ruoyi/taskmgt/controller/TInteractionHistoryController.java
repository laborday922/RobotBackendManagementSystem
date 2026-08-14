package com.ruoyi.taskmgt.controller;

import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletResponse;

import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.taskmgt.domain.TInteractionHistory;
import com.ruoyi.taskmgt.service.ITInteractionHistoryService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 交互历史记录Controller
 * 
 * @author xiaocai
 * @date 2026-03-13
 */
@RestController
@RequestMapping("/taskmgt/intHistory")
public class TInteractionHistoryController extends BaseController
{
    @Autowired
    private ITInteractionHistoryService tInteractionHistoryService;

    /**
     * 查询交互历史记录列表
     */
//    @PreAuthorize("@ss.hasPermi('taskmgt:intHistory:list')")
    @GetMapping("/list")
    @ApiOperation("查询交互历史记录列表")
    public TableDataInfo list(TInteractionHistory tInteractionHistory)
    {
        startPage();
        List<TInteractionHistory> list = tInteractionHistoryService.selectTInteractionHistoryList(tInteractionHistory);
        return getDataTable(list);
    }

    /**
     * 导出交互历史记录列表
     */
//    @PreAuthorize("@ss.hasPermi('taskmgt:intHistory:export')")
    @Log(title = "交互历史记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TInteractionHistory tInteractionHistory)
    {
        List<TInteractionHistory> list = tInteractionHistoryService.selectTInteractionHistoryList(tInteractionHistory);
        ExcelUtil<TInteractionHistory> util = new ExcelUtil<TInteractionHistory>(TInteractionHistory.class);
        util.exportExcel(response, list, "交互历史记录数据");
    }

    /**
     * 获取交互历史记录详细信息
     */
//    @PreAuthorize("@ss.hasPermi('taskmgt:intHistory:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(tInteractionHistoryService.selectTInteractionHistoryById(id));
    }

    /**
     * 新增交互历史记录
     */
    @ApiOperation("新增交互历史记录列表")
//    @PreAuthorize("@ss.hasPermi('taskmgt:intHistory:add')")
    @Log(title = "交互历史记录", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TInteractionHistory tInteractionHistory)
    {
        return toAjax(tInteractionHistoryService.insertTInteractionHistory(tInteractionHistory));
    }

    /**
     * 交互历史记录列表汇总
     */
    @GetMapping("/list/sumof")
    @ApiOperation("交互历史记录列表汇总")
    public AjaxResult sumOfList()
    {
        return success(tInteractionHistoryService.sumOfInteractionHistory());
    }

    /**
     * 提交任务评价（评分 + 评价内容），根据 interactionId 从 TaskLog 获取时间和状态
     */
    @ApiOperation("提交任务评价")
    @PostMapping("/evaluate")
    public AjaxResult evaluate(
            @RequestParam String interactionId,
            @RequestParam Long rating,
            @RequestParam(required = false) String evaluationText) {

        try {
            int rows = tInteractionHistoryService.buildAndSaveEvaluation(interactionId, rating, evaluationText);
            return rows > 0 ? success() : error("提交评价失败");
        } catch (IllegalArgumentException e) {
            return error(e.getMessage());
        }
    }

    /**
     * 提交问答评价（评分 + 评价内容 + 交互时间相关字段），与任务无关，无需 interactionId
     */
    @ApiOperation("提交问答评价")
    @PostMapping("/QAevaluate")
    public AjaxResult qaEvaluate(
            @RequestParam Long rating,
            @RequestParam(required = false) String evaluationText,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date interactionTime,
            @RequestParam(required = false) Long duration) {
        int rows = tInteractionHistoryService.saveQAEvaluation(rating, evaluationText, interactionTime, duration);
        return rows > 0 ? success() : error("提交评价失败");
    }

//    /**
//     * 修改交互历史记录
//     */
//    @PreAuthorize("@ss.hasPermi('taskmgt:intHistory:edit')")
//    @Log(title = "交互历史记录", businessType = BusinessType.UPDATE)
//    @PutMapping
//    public AjaxResult edit(@RequestBody TInteractionHistory tInteractionHistory)
//    {
//        return toAjax(tInteractionHistoryService.updateTInteractionHistory(tInteractionHistory));
//    }

//    /**
//     * 删除交互历史记录
//     */
//    @PreAuthorize("@ss.hasPermi('taskmgt:intHistory:remove')")
//    @Log(title = "交互历史记录", businessType = BusinessType.DELETE)
//	@DeleteMapping("/{ids}")
//    public AjaxResult remove(@PathVariable Long[] ids)
//    {
//        return toAjax(tInteractionHistoryService.deleteTInteractionHistoryByIds(ids));
//    }
}
