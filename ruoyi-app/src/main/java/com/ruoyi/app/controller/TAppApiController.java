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
import com.ruoyi.app.domain.TAppApi;
import com.ruoyi.app.service.ITAppApiService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 支持的APIController
 * 
 * @author xiaocai
 * @date 2026-03-28
 */
@RestController
@RequestMapping("/app/api")
public class TAppApiController extends BaseController
{
    @Autowired
    private ITAppApiService tAppApiService;

    /**
     * 查询支持的API列表
     */
//    @PreAuthorize("@ss.hasPermi('app:api:list')")
    @GetMapping("/list")
    public TableDataInfo list(TAppApi tAppApi)
    {
        startPage();
        List<TAppApi> list = tAppApiService.selectTAppApiList(tAppApi);
        return getDataTable(list);
    }

    /**
     * 导出支持的API列表
     */
//    @PreAuthorize("@ss.hasPermi('app:api:export')")
    @Log(title = "支持的API", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TAppApi tAppApi)
    {
        List<TAppApi> list = tAppApiService.selectTAppApiList(tAppApi);
        ExcelUtil<TAppApi> util = new ExcelUtil<TAppApi>(TAppApi.class);
        util.exportExcel(response, list, "支持的API数据");
    }

    /**
     * 获取支持的API详细信息
     */
//    @PreAuthorize("@ss.hasPermi('app:api:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(tAppApiService.selectTAppApiById(id));
    }

    /**
     * 新增支持的API
     */
    @PreAuthorize("@ss.hasPermi('app:api:add')")
    @Log(title = "支持的API", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TAppApi tAppApi)
    {
        return toAjax(tAppApiService.insertTAppApi(tAppApi));
    }

    /**
     * 修改支持的API
     */
//    @PreAuthorize("@ss.hasPermi('app:api:edit')")
    @Log(title = "支持的API", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TAppApi tAppApi)
    {
        return toAjax(tAppApiService.updateTAppApi(tAppApi));
    }

    /**
     * 删除支持的API
     */
//    @PreAuthorize("@ss.hasPermi('app:api:remove')")
    @Log(title = "支持的API", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(tAppApiService.deleteTAppApiByIds(ids));
    }
}
