package com.ruoyi.app.controller;

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
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.app.domain.TAppParam;
import com.ruoyi.app.service.ITAppParamService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 应用能力参数定义Controller
 * 
 * @author xiaocai
 * @date 2026-03-28
 */
@RestController
@RequestMapping("/app/param")
public class TAppParamController extends BaseController
{
    @Autowired
    private ITAppParamService tAppParamService;

    /**
     * 查询应用能力参数定义列表
     */
//    @PreAuthorize("@ss.hasPermi('app:param:list')")
    @GetMapping("/list")
    public TableDataInfo list(TAppParam tAppParam)
    {
        startPage();
        List<TAppParam> list = tAppParamService.selectTAppParamList(tAppParam);
        return getDataTable(list);
    }

    /**
     * 导出应用能力参数定义列表
     */
//    @PreAuthorize("@ss.hasPermi('app:param:export')")
    @Log(title = "应用能力参数定义", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TAppParam tAppParam)
    {
        List<TAppParam> list = tAppParamService.selectTAppParamList(tAppParam);
        ExcelUtil<TAppParam> util = new ExcelUtil<TAppParam>(TAppParam.class);
        util.exportExcel(response, list, "应用能力参数定义数据");
    }

    /**
     * 获取应用能力参数定义详细信息
     */
//    @PreAuthorize("@ss.hasPermi('app:param:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(tAppParamService.selectTAppParamById(id));
    }

    /**
     * 新增应用能力参数定义
     */
//    @PreAuthorize("@ss.hasPermi('app:param:add')")
    @Log(title = "应用能力参数定义", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TAppParam tAppParam)
    {
        return toAjax(tAppParamService.insertTAppParam(tAppParam));
    }

    /**
     * 修改应用能力参数定义
     */
//    @PreAuthorize("@ss.hasPermi('app:param:edit')")
    @Log(title = "应用能力参数定义", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TAppParam tAppParam)
    {
        return toAjax(tAppParamService.updateTAppParam(tAppParam));
    }

    /**
     * 删除应用能力参数定义
     */
//    @PreAuthorize("@ss.hasPermi('app:param:remove')")
    @Log(title = "应用能力参数定义", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(tAppParamService.deleteTAppParamByIds(ids));
    }
}
