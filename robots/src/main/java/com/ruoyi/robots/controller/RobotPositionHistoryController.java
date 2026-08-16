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
//    @PreAuthorize("@ss.hasPermi('robots:history:list')")
    @GetMapping("/list")
    public TableDataInfo list(RobotPositionHistory robotPositionHistory)
    {
        startPage();
        List<RobotPositionHistory> list = robotPositionHistoryService.selectRobotPositionHistoryList(robotPositionHistory);
        return getDataTable(list);
    }

    /**
     * 获取机器人位置历史信息详细信息
     *
     */
    @ApiOperation("获取机器人位置历史信息详细信息")
//    @PreAuthorize("@ss.hasPermi('robots:history:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(robotPositionHistoryService.selectRobotPositionHistoryById(id));
    }

}
