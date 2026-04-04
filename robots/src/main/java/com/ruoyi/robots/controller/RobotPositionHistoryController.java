package com.ruoyi.robots.controller;

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
import com.ruoyi.robots.domain.RobotPositionHistory;
import com.ruoyi.robots.service.IRobotPositionHistoryService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 机器人位置历史信息Controller
 * 
 * @author xiaocai
 * @date 2026-03-07
 */
@RestController
@RequestMapping("/robots/history")
public class RobotPositionHistoryController extends BaseController
{
    @Autowired
    private IRobotPositionHistoryService robotPositionHistoryService;

    /**
     * 查询机器人位置历史信息列表
     */
    @ApiOperation("查询机器人位置历史信息列表")
    @PreAuthorize("@ss.hasPermi('robots:history:list')")
    @GetMapping("/list")
    public TableDataInfo list(RobotPositionHistory robotPositionHistory)
    {
        startPage();
        List<RobotPositionHistory> list = robotPositionHistoryService.selectRobotPositionHistoryList(robotPositionHistory);
        return getDataTable(list);
    }

    /**
     * 导出机器人位置历史信息列表
     */
    @PreAuthorize("@ss.hasPermi('robots:history:export')")
    @Log(title = "机器人位置历史信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, RobotPositionHistory robotPositionHistory)
    {
        List<RobotPositionHistory> list = robotPositionHistoryService.selectRobotPositionHistoryList(robotPositionHistory);
        ExcelUtil<RobotPositionHistory> util = new ExcelUtil<RobotPositionHistory>(RobotPositionHistory.class);
        util.exportExcel(response, list, "机器人位置历史信息数据");
    }

    /**
     * 获取机器人位置历史信息详细信息
     *
     */
    @ApiOperation("获取机器人位置历史信息详细信息")
    @PreAuthorize("@ss.hasPermi('robots:history:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(robotPositionHistoryService.selectRobotPositionHistoryById(id));
    }

    /**
     * 新增机器人位置历史信息
     *
     */
    @ApiOperation("新增机器人位置历史信息")
    @PreAuthorize("@ss.hasPermi('robots:history:add')")
    @Log(title = "机器人位置历史信息", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody RobotPositionHistory robotPositionHistory)
    {
        return toAjax(robotPositionHistoryService.insertRobotPositionHistory(robotPositionHistory));
    }

//    /**
//     * 修改机器人位置历史信息
//     */
//    @PreAuthorize("@ss.hasPermi('robots:history:edit')")
//    @Log(title = "机器人位置历史信息", businessType = BusinessType.UPDATE)
//    @PutMapping
//    public AjaxResult edit(@RequestBody RobotPositionHistory robotPositionHistory)
//    {
//        return toAjax(robotPositionHistoryService.updateRobotPositionHistory(robotPositionHistory));
//    }
//
//    /**
//     * 删除机器人位置历史信息
//     */
//    @PreAuthorize("@ss.hasPermi('robots:history:remove')")
//    @Log(title = "机器人位置历史信息", businessType = BusinessType.DELETE)
//	@DeleteMapping("/{ids}")
//    public AjaxResult remove(@PathVariable Long[] ids)
//    {
//        return toAjax(robotPositionHistoryService.deleteRobotPositionHistoryByIds(ids));
//    }
}
