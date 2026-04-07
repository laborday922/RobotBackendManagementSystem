package com.ruoyi.mode.controller;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.mode.domain.SysModeHistory;
import com.ruoyi.mode.service.ISysModeHistoryService;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * 模式切换历史记录Controller
 *
 * @author ruoyi
 */
@Api(value = "模式切换历史记录", tags = {"模式切换历史记录管理"})
@RestController
@RequestMapping("/system/history")
public class SysModeHistoryController extends BaseController
{
    @Autowired
    private ISysModeHistoryService sysModeHistoryService;

    /**
     * 查询历史记录列表
     */
    @PreAuthorize("@ss.hasPermi('system:history:list')")
    @ApiOperation(value = "查询历史记录列表", notes = "分页查询模式切换历史记录列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageNum", value = "页码", dataType = "int", paramType = "query", defaultValue = "1"),
            @ApiImplicitParam(name = "pageSize", value = "每页条数", dataType = "int", paramType = "query", defaultValue = "10")
    })
    @GetMapping("/list")
    public TableDataInfo list(@ApiParam(value = "历史记录查询条件") SysModeHistory sysModeHistory)
    {
        startPage();
        List<SysModeHistory> list = sysModeHistoryService.selectSysModeHistoryList(sysModeHistory);
        return getDataTable(list);
    }

    /**
     * 导出历史记录列表
     */
    @PreAuthorize("@ss.hasPermi('system:history:export')")
    @Log(title = "历史记录", businessType = BusinessType.EXPORT)
    @ApiOperation(value = "导出历史记录列表", notes = "导出模式切换历史记录列表到Excel")
    @PostMapping("/export")
    public void export(HttpServletResponse response,
                       @ApiParam(value = "历史记录查询条件") SysModeHistory sysModeHistory)
    {
        List<SysModeHistory> list = sysModeHistoryService.selectSysModeHistoryList(sysModeHistory);
        ExcelUtil<SysModeHistory> util = new ExcelUtil<SysModeHistory>(SysModeHistory.class);
        util.exportExcel(response, list, "历史记录数据");
    }

    /**
     * 获取历史记录详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:history:query')")
    @ApiOperation(value = "获取历史记录详细信息", notes = "根据历史记录ID获取详细信息")
    @ApiImplicitParam(name = "historyId", value = "历史记录ID", required = true, dataType = "Long", paramType = "path")
    @GetMapping(value = "/{historyId}")
    public AjaxResult getInfo(@PathVariable("historyId") Long historyId)
    {
        return success(sysModeHistoryService.selectSysModeHistoryById(historyId));
    }

    /**
     * 新增历史记录
     */
    @PreAuthorize("@ss.hasPermi('system:history:add')")
    @Log(title = "历史记录", businessType = BusinessType.INSERT)
    @ApiOperation(value = "新增历史记录", notes = "添加一条模式切换历史记录")
    @PostMapping
    public AjaxResult add(@RequestBody SysModeHistory sysModeHistory)
    {
        return toAjax(sysModeHistoryService.insertSysModeHistory(sysModeHistory));
    }

    /**
     * 删除历史记录（批量）
     */
    @PreAuthorize("@ss.hasPermi('system:history:remove')")
    @Log(title = "历史记录", businessType = BusinessType.DELETE)
    @ApiOperation(value = "删除历史记录", notes = "根据历史记录ID数组批量删除历史记录")
    @ApiImplicitParam(name = "historyIds", value = "历史记录ID数组", required = true, dataType = "Long[]", paramType = "path", allowMultiple = true)
    @DeleteMapping("/{historyIds}")
    public AjaxResult remove(@PathVariable Long[] historyIds)
    {
        return toAjax(sysModeHistoryService.deleteSysModeHistoryByIds(historyIds));
    }

    /**
     * 清空所有历史记录
     */
    @PreAuthorize("@ss.hasPermi('system:history:remove')")
    @Log(title = "历史记录", businessType = BusinessType.DELETE)
    @ApiOperation(value = "清空所有历史记录", notes = "清空所有的模式切换历史记录")
    @DeleteMapping("/clear")
    public AjaxResult clear()
    {
        return toAjax(sysModeHistoryService.clearAllHistory());
    }

    /**
     * 获取操作类型统计（各类型操作数量）
     */
    @PreAuthorize("@ss.hasPermi('system:history:list')")
    @ApiOperation(value = "操作类型统计", notes = "统计各操作类型的记录数量")
    @GetMapping("/stats/operationType")
    public AjaxResult getOperationTypeStats()
    {
        List<Map<String, Object>> stats = sysModeHistoryService.selectOperationTypeStats();
        return success(stats);
    }

    /**
     * 获取机器人操作统计（各机器人操作次数）
     */
    @PreAuthorize("@ss.hasPermi('system:history:list')")
    @ApiOperation(value = "机器人操作统计", notes = "统计各机器人的操作次数")
    @GetMapping("/stats/robot")
    public AjaxResult getRobotOperationStats()
    {
        List<Map<String, Object>> stats = sysModeHistoryService.selectRobotOperationStats();
        return success(stats);
    }

    /**
     * 获取每日操作统计（最近N天）
     */
    @PreAuthorize("@ss.hasPermi('system:history:list')")
    @ApiOperation(value = "每日操作统计", notes = "统计最近N天的操作数量，默认7天")
    @ApiImplicitParam(name = "days", value = "最近天数", dataType = "int", paramType = "query", defaultValue = "7")
    @GetMapping("/stats/daily")
    public AjaxResult getDailyOperationStats(@RequestParam(defaultValue = "7") Integer days)
    {
        List<Map<String, Object>> stats = sysModeHistoryService.selectDailyOperationStats(days);
        return success(stats);
    }
}