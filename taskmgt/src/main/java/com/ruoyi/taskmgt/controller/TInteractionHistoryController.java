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
     * 提交任务评价（评分 + 评价内容），根据 interactionId 从 TaskLog 获取时间和状态
     */
    @ApiOperation("提交任务评价")
    @PostMapping("/evaluate")
    public AjaxResult evaluate(
            @RequestParam String interactionId,
            @RequestParam Long rating,
            @RequestParam(required = false) String evaluationText) {

        // 1. 查询该 interactionId 对应的所有日志（按时间升序）
        List<TaskLog> logs = taskLogRepository.findByInteractionId(interactionId);
        if (logs.isEmpty()) {
            return error("未找到 interactionId=" + interactionId + " 的任务执行日志");
        }

        // 2. 第一条日志的时间为交互开始时间，最后一条的事件类型决定状态
        TaskLog firstLog = logs.get(0);
        TaskLog lastLog = logs.get(logs.size() - 1);

        // 3. 构建交互历史记录
        TInteractionHistory record = new TInteractionHistory();
        record.setTaskId(firstLog.getTaskId().toString());
        record.setInteractionId(interactionId);
        record.setInteractionTime(firstLog.getCreateTime());

        // 成功：最后一条是 TASK_COMPLETE；失败：最后一条是 TASK_TERMINATE
        String lastEventType = lastLog.getEventType();
        if ("TASK_TERMINATE".equals(lastEventType)) {
            record.setStatus(1L); // 失败
        } else if ("TASK_COMPLETE".equals(lastEventType)) {
            record.setStatus(0L); // 成功
        } else {
            record.setStatus(0L); // 默认成功
        }

        // 计算耗时（秒）
        long durationSeconds = (lastLog.getCreateTime().getTime() - firstLog.getCreateTime().getTime()) / 1000;
        record.setDuration(durationSeconds);

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
