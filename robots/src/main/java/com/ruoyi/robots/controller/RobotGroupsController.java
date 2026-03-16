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
import com.ruoyi.robots.domain.RobotGroups;
import com.ruoyi.robots.service.IRobotGroupsService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 机器人分组Controller
 * 
 * @author xiaocai
 * @date 2026-03-07
 */
@RestController
@RequestMapping("/robots/groups")
public class RobotGroupsController extends BaseController
{
    @Autowired
    private IRobotGroupsService robotGroupsService;

    /**
     * 查询机器人分组列表
     */
    @ApiOperation("查询机器人分组列表")
    @PreAuthorize("@ss.hasPermi('robots:groups:list')")
    @GetMapping("/list")
    public TableDataInfo list(RobotGroups robotGroups)
    {
        startPage();
        List<RobotGroups> list = robotGroupsService.selectRobotGroupsList(robotGroups);
        return getDataTable(list);
    }

    /**
     * 导出机器人分组列表
     */
    @PreAuthorize("@ss.hasPermi('robots:groups:export')")
    @Log(title = "机器人分组", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, RobotGroups robotGroups)
    {
        List<RobotGroups> list = robotGroupsService.selectRobotGroupsList(robotGroups);
        ExcelUtil<RobotGroups> util = new ExcelUtil<RobotGroups>(RobotGroups.class);
        util.exportExcel(response, list, "机器人分组数据");
    }

    /**
     * 获取机器人分组详细信息
     */
    @PreAuthorize("@ss.hasPermi('robots:groups:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(robotGroupsService.selectRobotGroupsById(id));
    }

    /**
     * 新增机器人分组
     * 前端只接受两个参数，分组名称(name)和分组描述(description)
     */
    @ApiOperation("新增机器人分组")
    @PreAuthorize("@ss.hasPermi('robots:groups:add')")
    @Log(title = "机器人分组", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody RobotGroups robotGroups)
    {
        return toAjax(robotGroupsService.insertRobotGroups(robotGroups));
    }

    /**
     * 修改机器人分组
     * 前端只接受两个参数，分组名称(name)和分组描述(description)
     */
    @ApiOperation("修改机器人分组")
    @PreAuthorize("@ss.hasPermi('robots:groups:edit')")
    @Log(title = "机器人分组", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody RobotGroups robotGroups)
    {

        return toAjax(robotGroupsService.updateRobotGroups(robotGroups));
    }

    /**
     * 删除机器人分组
     */
    @ApiOperation("删除机器人分组")
    @PreAuthorize("@ss.hasPermi('robots:groups:remove')")
    @Log(title = "机器人分组", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(robotGroupsService.deleteRobotGroupsByIds(ids));
    }

    //todo 开启/关闭机器人分组
}
