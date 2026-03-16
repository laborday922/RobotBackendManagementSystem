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
import com.ruoyi.app.domain.TAppUpdate;
import com.ruoyi.app.service.ITAppUpdateService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 应用更新记录Controller
 * 
 * @author xiaocai
 * @date 2026-03-13
 */
@RestController
@RequestMapping("/app/appUpdate")
public class TAppUpdateController extends BaseController
{
    @Autowired
    private ITAppUpdateService tAppUpdateService;

    /**
     * 查询应用更新记录列表
     */
    @PreAuthorize("@ss.hasPermi('app:appUpdate:list')")
    @GetMapping("/list")
    @ApiOperation("查询应用更新记录列表")
    public TableDataInfo list(TAppUpdate tAppUpdate)
    {
        startPage();
        List<TAppUpdate> list = tAppUpdateService.selectTAppUpdateList(tAppUpdate);
        return getDataTable(list);
    }

    /**
     * 导出应用更新记录列表
     */
    @PreAuthorize("@ss.hasPermi('app:appUpdate:export')")
    @Log(title = "应用更新记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TAppUpdate tAppUpdate)
    {
        List<TAppUpdate> list = tAppUpdateService.selectTAppUpdateList(tAppUpdate);
        ExcelUtil<TAppUpdate> util = new ExcelUtil<TAppUpdate>(TAppUpdate.class);
        util.exportExcel(response, list, "应用更新记录数据");
    }

    /**
     * 获取应用更新记录详细信息
     */
    @ApiOperation("获取应用更新记录详细信息")
    @PreAuthorize("@ss.hasPermi('app:appUpdate:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(tAppUpdateService.selectTAppUpdateById(id));
    }

    /**
     * 新增应用更新记录
     */
    @PreAuthorize("@ss.hasPermi('app:appUpdate:add')")
    @ApiOperation("新增应用更新记录")
    @Log(title = "应用更新记录", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TAppUpdate tAppUpdate)
    {
        return toAjax(tAppUpdateService.insertTAppUpdate(tAppUpdate));
    }



//    /**
//     * 修改应用更新记录
//     */
//    @PreAuthorize("@ss.hasPermi('app:appUpdate:edit')")
//    @Log(title = "应用更新记录", businessType = BusinessType.UPDATE)
//    @PutMapping
//    public AjaxResult edit(@RequestBody TAppUpdate tAppUpdate)
//    {
//        return toAjax(tAppUpdateService.updateTAppUpdate(tAppUpdate));
//    }
//
//    /**
//     * 删除应用更新记录
//     */
//    @PreAuthorize("@ss.hasPermi('app:appUpdate:remove')")
//    @Log(title = "应用更新记录", businessType = BusinessType.DELETE)
//	@DeleteMapping("/{ids}")
//    public AjaxResult remove(@PathVariable Long[] ids)
//    {
//        return toAjax(tAppUpdateService.deleteTAppUpdateByIds(ids));
//    }
}
