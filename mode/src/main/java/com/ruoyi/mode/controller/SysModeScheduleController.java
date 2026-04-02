package com.ruoyi.mode.controller;

import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.mode.domain.SysModeSchedule;
import com.ruoyi.mode.service.ISysModeScheduleService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;

/**
 * 模式排程Controller
 *
 * @author ruoyi
 */
@Api(value = "模式排程管理", tags = {"模式排程管理接口"})
@RestController
@RequestMapping("/system/schedule")
public class SysModeScheduleController extends BaseController
{
    @Autowired
    private ISysModeScheduleService sysModeScheduleService;

    /**
     * 查询排程列表
     */
    @PreAuthorize("@ss.hasPermi('system:schedule:list')")
    @ApiOperation(value = "查询排程列表", notes = "分页查询模式排程列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageNum", value = "页码", dataType = "int", paramType = "query", defaultValue = "1"),
            @ApiImplicitParam(name = "pageSize", value = "每页条数", dataType = "int", paramType = "query", defaultValue = "10")
    })
    @GetMapping("/list")
    public TableDataInfo list(@ApiParam(value = "排程查询条件") SysModeSchedule sysModeSchedule)
    {
        startPage();
        List<SysModeSchedule> list = sysModeScheduleService.selectSysModeScheduleList(sysModeSchedule);
        return getDataTable(list);
    }

    /**
     * 导出排程列表
     */
    @PreAuthorize("@ss.hasPermi('system:schedule:export')")
    @Log(title = "模式排程", businessType = BusinessType.EXPORT)
    @ApiOperation(value = "导出排程列表", notes = "导出模式排程列表到Excel")
    @PostMapping("/export")
    public void export(HttpServletResponse response,
                       @ApiParam(value = "排程查询条件") SysModeSchedule sysModeSchedule)
    {
        List<SysModeSchedule> list = sysModeScheduleService.selectSysModeScheduleList(sysModeSchedule);
        ExcelUtil<SysModeSchedule> util = new ExcelUtil<SysModeSchedule>(SysModeSchedule.class);
        util.exportExcel(response, list, "模式排程数据");
    }

    /**
     * 获取排程详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:schedule:query')")
    @ApiOperation(value = "获取排程详细信息", notes = "根据排程ID获取详细信息")
    @ApiImplicitParam(name = "scheduleId", value = "排程ID", required = true, dataType = "Long", paramType = "path")
    @GetMapping(value = "/{scheduleId}")
    public AjaxResult getInfo(@PathVariable("scheduleId") Long scheduleId)
    {
        return success(sysModeScheduleService.selectSysModeScheduleById(scheduleId));
    }

    /**
     * 新增排程
     */
    @PreAuthorize("@ss.hasPermi('system:schedule:add')")
    @Log(title = "模式排程", businessType = BusinessType.INSERT)
    @ApiOperation(value = "新增排程", notes = "创建新的模式排程")
    @PostMapping
    public AjaxResult add(@ApiParam(value = "排程信息", required = true) @RequestBody SysModeSchedule sysModeSchedule)
    {
        return toAjax(sysModeScheduleService.insertSysModeSchedule(sysModeSchedule));
    }

    /**
     * 修改排程
     */
    @PreAuthorize("@ss.hasPermi('system:schedule:edit')")
    @Log(title = "模式排程", businessType = BusinessType.UPDATE)
    @ApiOperation(value = "修改排程", notes = "修改现有的模式排程信息")
    @PutMapping
    public AjaxResult edit(@ApiParam(value = "排程信息", required = true) @RequestBody SysModeSchedule sysModeSchedule)
    {
        return toAjax(sysModeScheduleService.updateSysModeSchedule(sysModeSchedule));
    }

    /**
     * 删除排程
     */
    @PreAuthorize("@ss.hasPermi('system:schedule:remove')")
    @Log(title = "模式排程", businessType = BusinessType.DELETE)
    @ApiOperation(value = "删除排程", notes = "根据排程ID数组批量删除模式排程")
    @ApiImplicitParam(name = "scheduleIds", value = "排程ID数组", required = true, dataType = "Long[]", paramType = "path", allowMultiple = true)
    @DeleteMapping("/{scheduleIds}")
    public AjaxResult remove(@PathVariable Long[] scheduleIds)
    {
        return toAjax(sysModeScheduleService.deleteSysModeScheduleByIds(scheduleIds));
    }

    /**
     * 切换排程状态
     */
    @PreAuthorize("@ss.hasPermi('system:schedule:edit')")
    @Log(title = "模式排程", businessType = BusinessType.UPDATE)
    @ApiOperation(value = "切换排程状态", notes = "切换模式排程的启用/禁用状态")
    @ApiImplicitParam(name = "scheduleId", value = "排程ID", required = true, dataType = "Long", paramType = "path")
    @PutMapping("/toggleStatus/{scheduleId}")
    public AjaxResult toggleStatus(@PathVariable Long scheduleId)
    {
        return toAjax(sysModeScheduleService.toggleScheduleStatus(scheduleId));
    }

    /**
     * 获取日历数据
     */
    @PreAuthorize("@ss.hasPermi('system:schedule:list')")
    @GetMapping("/calendar")
    public AjaxResult getCalendarData(@RequestParam(required = false) Integer year)
    {
        if (year == null) {
            year = 2026; // 默认从2026年开始
        }
        // TODO: 从数据库查询该年份的任务完成情况
        // 返回数据格式示例：
        // {
        //   "2026-01-01": {"success": 2, "partial": 1, "failed": 0, "tasks": [...]}
        // }
        return success();
    }
}