package com.ruoyi.robots.controller;

import java.util.List;
import javax.servlet.http.HttpServletResponse;

import com.ruoyi.robots.controller.dto.RobotsDto;
import com.ruoyi.robots.domain.Robot;
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
import com.ruoyi.robots.service.IRobotsService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 机器人基础信息Controller
 *
 * @author xiaocai
 * @date 2026-03-07
 */
@RestController
@RequestMapping("/robots/robots")
public class RobotsController extends BaseController
{
    @Autowired
    private IRobotsService robotsService;

    /**
     * 查询机器人基础信息列表
     * groupId status hardwareStatus taskStatus
     */

    @PreAuthorize("@ss.hasPermi('robots:robots:list')")
    @GetMapping("/list")
    @ApiOperation("查询机器人列表")
    public TableDataInfo list(Robot robot)
    {
        startPage();
        List<Robot> list = robotsService.selectRobotsList(robot);
        return getDataTable(list);
    }

    /**
     * 导出机器人基础信息列表
     */
    @PreAuthorize("@ss.hasPermi('robots:robots:export')")
    @Log(title = "机器人基础信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Robot robot)
    {
        List<Robot> list = robotsService.selectRobotsList(robot);
        ExcelUtil<Robot> util = new ExcelUtil<Robot>(Robot.class);
        util.exportExcel(response, list, "机器人基础信息数据");
    }

    /**
     * 获取机器人基础信息详细信息
     * todo 机器人的基础信息有一部分是通过外部调用得到的，暂时设定为从数据库读取
     */
    @ApiOperation("机器人基础信息详细信息")
    @PreAuthorize("@ss.hasPermi('robots:robots:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(robotsService.selectRobotsById(id));
    }

    /**
     * 新增机器人基础信息
     *
     */
    @ApiOperation("新增机器人基础信息")
    @PreAuthorize("@ss.hasPermi('robots:robots:add')")
    @Log(title = "机器人基础信息", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Robot robot)
    {
        return toAjax(robotsService.insertRobots(robot));
    }

    /**
     * 修改机器人基础信息
     */
    @ApiOperation("修改机器人基础信息")
    @PreAuthorize("@ss.hasPermi('robots:robots:edit')")
    @Log(title = "机器人基础信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody RobotsDto robotsDto)
    {
        return toAjax(robotsService.updateRobots(robotsDto));
    }

    /**
     * 删除机器人基础信息
     */
    @ApiOperation("删除机器人基础信息")
    @PreAuthorize("@ss.hasPermi('robots:robots:remove')")
    @Log(title = "机器人基础信息", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids)
    {
        return toAjax(robotsService.deleteRobotsByIds(ids));
    }
}
