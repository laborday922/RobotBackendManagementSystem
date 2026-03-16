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
import com.ruoyi.app.domain.TAppConfig;
import com.ruoyi.app.service.ITAppConfigService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 应用配置Controller
 * 
 * @author xiaocai
 * @date 2026-03-13
 */
@RestController
@RequestMapping("/app/appConfig")
public class TAppConfigController extends BaseController
{
    @Autowired
    private ITAppConfigService tAppConfigService;

    /**
     * 查询应用配置列表
     */
    @ApiOperation("查询应用配置列表")
    @PreAuthorize("@ss.hasPermi('app:appConfig:list')")
    @GetMapping("/list")
    public TableDataInfo list(TAppConfig tAppConfig)
    {
        startPage();
        List<TAppConfig> list = tAppConfigService.selectTAppConfigList(tAppConfig);
        return getDataTable(list);
    }

    /**
     * 导出应用配置列表
     */
    @PreAuthorize("@ss.hasPermi('app:appConfig:export')")
    @Log(title = "应用配置", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TAppConfig tAppConfig)
    {
        List<TAppConfig> list = tAppConfigService.selectTAppConfigList(tAppConfig);
        ExcelUtil<TAppConfig> util = new ExcelUtil<TAppConfig>(TAppConfig.class);
        util.exportExcel(response, list, "应用配置数据");
    }

    /**
     * 获取应用配置详细信息
     */
    @ApiOperation("获取应用配置详细信息")
    @PreAuthorize("@ss.hasPermi('app:appConfig:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(tAppConfigService.selectTAppConfigById(id));
    }

    /**
     * 新增应用配置
     */
    @ApiOperation("新增应用配置")
    @PreAuthorize("@ss.hasPermi('app:appConfig:add')")
    @Log(title = "应用配置", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TAppConfig tAppConfig)
    {
        return toAjax(tAppConfigService.insertTAppConfig(tAppConfig));
    }

    /**
     * 修改应用配置
     */
    @ApiOperation("修改应用配置")
    @PreAuthorize("@ss.hasPermi('app:appConfig:edit')")
    @Log(title = "应用配置", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TAppConfig tAppConfig)
    {
        return toAjax(tAppConfigService.updateTAppConfig(tAppConfig));
    }

    /**
     * 删除应用配置
     */
    @ApiOperation("删除应用配置")
    @PreAuthorize("@ss.hasPermi('app:appConfig:remove')")
    @Log(title = "应用配置", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(tAppConfigService.deleteTAppConfigByIds(ids));
    }
}
