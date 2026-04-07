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
import com.ruoyi.app.domain.TAppConstraint;
import com.ruoyi.app.service.ITAppConstraintService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 应用级约束规则Controller
 * 
 * @author xiaocai
 * @date 2026-03-28
 */
@RestController
@RequestMapping("/app/constraint")
public class TAppConstraintController extends BaseController
{
    @Autowired
    private ITAppConstraintService tAppConstraintService;

    /**
     * 查询应用级约束规则列表
     */
//    @PreAuthorize("@ss.hasPermi('app:constraint:list')")
    @GetMapping("/list")
    public TableDataInfo list(TAppConstraint tAppConstraint)
    {
        startPage();
        List<TAppConstraint> list = tAppConstraintService.selectTAppConstraintList(tAppConstraint);
        return getDataTable(list);
    }

    /**
     * 导出应用级约束规则列表
     */
//    @PreAuthorize("@ss.hasPermi('app:constraint:export')")
    @Log(title = "应用级约束规则", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TAppConstraint tAppConstraint)
    {
        List<TAppConstraint> list = tAppConstraintService.selectTAppConstraintList(tAppConstraint);
        ExcelUtil<TAppConstraint> util = new ExcelUtil<TAppConstraint>(TAppConstraint.class);
        util.exportExcel(response, list, "应用级约束规则数据");
    }

    /**
     * 获取应用级约束规则详细信息
     */
//    @PreAuthorize("@ss.hasPermi('app:constraint:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(tAppConstraintService.selectTAppConstraintById(id));
    }

    /**
     * 新增应用级约束规则
     */
//    @PreAuthorize("@ss.hasPermi('app:constraint:add')")
    @Log(title = "应用级约束规则", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TAppConstraint tAppConstraint)
    {
        return toAjax(tAppConstraintService.insertTAppConstraint(tAppConstraint));
    }

    /**
     * 修改应用级约束规则
     */
//    @PreAuthorize("@ss.hasPermi('app:constraint:edit')")
    @Log(title = "应用级约束规则", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TAppConstraint tAppConstraint)
    {
        return toAjax(tAppConstraintService.updateTAppConstraint(tAppConstraint));
    }

    /**
     * 删除应用级约束规则
     */
//    @PreAuthorize("@ss.hasPermi('app:constraint:remove')")
    @Log(title = "应用级约束规则", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(tAppConstraintService.deleteTAppConstraintByIds(ids));
    }
}
