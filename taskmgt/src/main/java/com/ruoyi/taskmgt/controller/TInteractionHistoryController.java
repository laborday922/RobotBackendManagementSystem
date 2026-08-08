package com.ruoyi.taskmgt.controller;

import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletResponse;

import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
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
import com.ruoyi.taskmgt.domain.TaskLogRepository;
import com.ruoyi.taskmgt.domain.bo.TaskLog;
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

    @Autowired
    private TaskLogRepository taskLogRepository;

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
     * 提交任务评价（评分 + 评价内容），从 TaskLog 自动获取时间和交互状态
     */
    @ApiOperation("提交任务评价")
    @PostMapping("/evaluate")
    public AjaxResult evaluate(
            @RequestParam Long taskId,
            @RequestParam Long rating,
            @RequestParam(required = false) String evaluationText) {
        // 1. 查询该任务最新一条带 interactionId 的执行日志
        TaskLog latestLog = taskLogRepository.findLatestWithInteractionId(taskId)
                .orElse(null);

        // 2. 构建交互历史记录
        TInteractionHistory record = new TInteractionHistory();
        record.setTaskId(taskId.toString());

        if (latestLog != null) {
            record.setInteractionId(latestLog.getInteractionId());
            record.setInteractionTime(latestLog.getCreateTime());
            // 根据事件类型推断交互状态：TASK_COMPLETE→成功, TASK_TERMINATE→失败, 其他→成功
            String eventType = latestLog.getEventType();
            if ("TASK_TERMINATE".equals(eventType)) {
                record.setStatus(1L); // 失败
            } else if ("TASK_COMPLETE".equals(eventType)) {
                record.setStatus(0L); // 成功
            } else {
                record.setStatus(0L); // 默认成功
            }
        } else {
            // 无 interactionId 的日志时，生成新的
            record.setInteractionId(java.util.UUID.randomUUID().toString().replace("-", ""));
            record.setInteractionTime(new Date());
            record.setStatus(0L);
        }

        record.setRating(rating);
        record.setEvaluationText(evaluationText);
        // 其他字段留空：userId, userName, interactionType, interactionContent, extInfo 等

        int rows = tInteractionHistoryService.insertTInteractionHistory(record);
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
