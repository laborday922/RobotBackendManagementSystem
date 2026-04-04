package com.ruoyi.mode.controller;

import java.util.List;
import javax.servlet.http.HttpServletResponse;
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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.mode.domain.SysMode;
import com.ruoyi.mode.service.ISysModeService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 模式Controller
 *
 * @author ruoyi
 */
@Api(tags = "模式管理")
@RestController
@RequestMapping("/system/mode")
public class SysModeController extends BaseController
{
    @Autowired
    private ISysModeService sysModeService;

    /**
     * 查询模式列表
     */
    @ApiOperation("查询模式列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "modeName", value = "模式名称", dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "modeType", value = "模式类型(system/custom)", dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "enabled", value = "是否启用(0禁用 1启用)", dataType = "string", paramType = "query")
    })
    @PreAuthorize("@ss.hasPermi('system:mode:list')")
    @GetMapping("/list")
    public TableDataInfo list(SysMode sysMode)
    {
        startPage();
        List<SysMode> list = sysModeService.selectSysModeList(sysMode);
        return getDataTable(list);
    }

    /**
     * 导出模式列表
     */
    @ApiOperation("导出模式列表")
    @PreAuthorize("@ss.hasPermi('system:mode:export')")
    @Log(title = "模式", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, SysMode sysMode)
    {
        List<SysMode> list = sysModeService.selectSysModeList(sysMode);
        ExcelUtil<SysMode> util = new ExcelUtil<SysMode>(SysMode.class);
        util.exportExcel(response, list, "模式数据");
    }

    /**
     * 获取模式详细信息
     */
    @ApiOperation("获取模式详细信息")
    @ApiImplicitParam(
            name = "modeId",
            value = "模式ID",
            required = true,
            dataType = "int",
            paramType = "path",
            example = "1"
    )
    @PreAuthorize("@ss.hasPermi('system:mode:query')")
    @GetMapping(value = "/{modeId}")
    public AjaxResult getInfo(@PathVariable("modeId") Long modeId)
    {
        return success(sysModeService.selectSysModeById(modeId));
    }

    /**
     * 新增模式
     */
    @ApiOperation("新增模式")
    @ApiImplicitParam(
            name = "sysMode",
            value = "模式信息",
            required = true,
            dataType = "SysMode",
            paramType = "body"
    )
    @PreAuthorize("@ss.hasPermi('system:mode:add')")
    @Log(title = "模式", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody SysMode sysMode)
    {
        return toAjax(sysModeService.insertSysMode(sysMode));
    }

    /**
     * 修改模式
     */
    @ApiOperation("修改模式")
    @ApiImplicitParam(
            name = "sysMode",
            value = "模式信息",
            required = true,
            dataType = "SysMode",
            paramType = "body"
    )
    @PreAuthorize("@ss.hasPermi('system:mode:edit')")
    @Log(title = "模式", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody SysMode sysMode)
    {
        return toAjax(sysModeService.updateSysMode(sysMode));
    }

    /**
     * 删除模式
     */
    @ApiOperation("删除模式")
    @ApiImplicitParam(
            name = "modeIds",
            value = "模式ID数组（多个ID用逗号分隔）",
            required = true,
            dataType = "array",
            paramType = "path",
            example = "1,2,3"
    )
    @PreAuthorize("@ss.hasPermi('system:mode:remove')")
    @Log(title = "模式", businessType = BusinessType.DELETE)
    @DeleteMapping("/{modeIds}")
    public AjaxResult remove(@PathVariable Long[] modeIds)
    {
        return toAjax(sysModeService.deleteSysModeByIds(modeIds));
    }

    /**
     * 启用/禁用模式
     */
    @ApiOperation("启用/禁用模式")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "modeId", value = "模式ID", required = true, dataType = "int", paramType = "body"),
            @ApiImplicitParam(name = "enabled", value = "启用状态(0禁用 1启用)", required = true, dataType = "string", paramType = "body")
    })
    @PreAuthorize("@ss.hasPermi('system:mode:edit')")
    @Log(title = "模式", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus")
    public AjaxResult changeStatus(@RequestBody SysMode sysMode)
    {
        return toAjax(sysModeService.changeModeStatus(sysMode.getModeId(), sysMode.getEnabled()));
    }
}