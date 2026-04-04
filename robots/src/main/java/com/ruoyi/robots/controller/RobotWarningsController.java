package com.ruoyi.robots.controller;

import java.util.List;
import javax.servlet.http.HttpServletResponse;

import com.ruoyi.robots.common.RobotsConstants;
import com.ruoyi.robots.controller.dto.RobotWarningsDto;
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
import com.ruoyi.robots.domain.RobotWarnings;
import com.ruoyi.robots.service.IRobotWarningsService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 机器人状态预警Controller
 * 
 * @author xiaocai
 * @date 2026-03-07
 */
@RestController
@RequestMapping("/robots/warnings")
public class RobotWarningsController extends BaseController
{
    @Autowired
    private IRobotWarningsService robotWarningsService;

    /**
     * 查询机器人状态预警列表
     * 通过status的值来查询不同的结果
     */
    @ApiOperation("查询机器人状态预警历史")
    @PreAuthorize("@ss.hasPermi('robots:warnings:list')")
    @GetMapping("/list")
    public TableDataInfo listAll(RobotWarnings robotWarnings)
    {

        startPage();
        List<RobotWarnings> list = robotWarningsService.selectRobotWarningsList(robotWarnings);
        return getDataTable(list);
    }

    @ApiOperation("查询未处理机器人状态预警列表")
    @PreAuthorize("@ss.hasPermi('robots:warnings:list')")
    @GetMapping("/list/no")
    public TableDataInfo listNo()
    {

        startPage();
        List<RobotWarnings> list = robotWarningsService.selectRobotWarningsListByStatus(RobotsConstants.UNRESOLVED);
        return getDataTable(list);
    }
    @ApiOperation("查询已处理机器人状态预警列表")
    @PreAuthorize("@ss.hasPermi('robots:warnings:list')")
    @GetMapping("/list/yes")
    public TableDataInfo listYes()
    {

        startPage();
        List<RobotWarnings> list = robotWarningsService.selectRobotWarningsListByStatus(RobotsConstants.RESOLVED);
        return getDataTable(list);
    }
    /**
     * 导出机器人状态预警列表
     */
    @PreAuthorize("@ss.hasPermi('robots:warnings:export')")
    @Log(title = "机器人状态预警", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, RobotWarnings robotWarnings)
    {
        List<RobotWarnings> list = robotWarningsService.selectRobotWarningsList(robotWarnings);
        ExcelUtil<RobotWarnings> util = new ExcelUtil<RobotWarnings>(RobotWarnings.class);
        util.exportExcel(response, list, "机器人状态预警数据");
    }

    /**
     * 获取机器人状态预警详细信息
     */
    @PreAuthorize("@ss.hasPermi('robots:warnings:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(robotWarningsService.selectRobotWarningsById(id));
    }

    /**
     * 新增机器人状态预警
     *
     */
    @ApiOperation("新增机器人状态预警")
    @PreAuthorize("@ss.hasPermi('robots:warnings:add')")
    @Log(title = "机器人状态预警", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody RobotWarnings robotWarnings)
    {
        return toAjax(robotWarningsService.insertRobotWarnings(robotWarnings));
    }

//    /**
//     * 修改机器人状态预警
//     */
//    @PreAuthorize("@ss.hasPermi('robots:warnings:edit')")
//    @Log(title = "机器人状态预警", businessType = BusinessType.UPDATE)
//    @PutMapping
//    public AjaxResult edit(@RequestBody RobotWarnings robotWarnings)
//    {
//        return toAjax(robotWarningsService.updateRobotWarnings(robotWarnings));
//    }

//    /**
//     * 删除机器人状态预警
//     */
//    @PreAuthorize("@ss.hasPermi('robots:warnings:remove')")
//    @Log(title = "机器人状态预警", businessType = BusinessType.DELETE)
//	@DeleteMapping("/{ids}")
//    public AjaxResult remove(@PathVariable String[] ids)
//    {
//        return toAjax(robotWarningsService.deleteRobotWarningsByIds(ids));
//    }


    /**
     * 处理机器人状态预警
     */
    @ApiOperation("处理机器人状态预警")
    @PreAuthorize("@ss.hasPermi('robots:warnings:deal')")
    @Log(title = "机器人状态预警", businessType = BusinessType.UPDATE)
    @PutMapping("/deal")
    public AjaxResult deal(@RequestBody RobotWarningsDto robotWarningsDto)
    {
        return toAjax(robotWarningsService.dealRobotWarnings(robotWarningsDto));
    }
}
