package com.ruoyi.app.controller;

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
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.app.domain.TInteractionHistory;
import com.ruoyi.app.service.ITInteractionHistoryService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 交互历史记录Controller
 * 
 * @author xiaocai
 * @date 2026-03-13
 */
@RestController
@RequestMapping("/app/intHistory")
public class TInteractionHistoryController extends BaseController
{
    @Autowired
    private ITInteractionHistoryService tInteractionHistoryService;

    /**
     * 查询交互历史记录列表
     */
//    @PreAuthorize("@ss.hasPermi('app:intHistory:list')")
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
//    @PreAuthorize("@ss.hasPermi('app:intHistory:export')")
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
//    @PreAuthorize("@ss.hasPermi('app:intHistory:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(tInteractionHistoryService.selectTInteractionHistoryById(id));
    }

    /**
     * 新增交互历史记录
     */
    @ApiOperation("新增交互历史记录列表")
//    @PreAuthorize("@ss.hasPermi('app:intHistory:add')")
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



//    /**
//     * 修改交互历史记录
//     */
//    @PreAuthorize("@ss.hasPermi('app:intHistory:edit')")
//    @Log(title = "交互历史记录", businessType = BusinessType.UPDATE)
//    @PutMapping
//    public AjaxResult edit(@RequestBody TInteractionHistory tInteractionHistory)
//    {
//        return toAjax(tInteractionHistoryService.updateTInteractionHistory(tInteractionHistory));
//    }

//    /**
//     * 删除交互历史记录
//     */
//    @PreAuthorize("@ss.hasPermi('app:intHistory:remove')")
//    @Log(title = "交互历史记录", businessType = BusinessType.DELETE)
//	@DeleteMapping("/{ids}")
//    public AjaxResult remove(@PathVariable Long[] ids)
//    {
//        return toAjax(tInteractionHistoryService.deleteTInteractionHistoryByIds(ids));
//    }
}
