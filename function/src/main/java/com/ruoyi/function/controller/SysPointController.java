package com.ruoyi.function.controller;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.function.domain.SysPoint;
import com.ruoyi.function.service.ISysPointService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "点位管理")
@RestController
@RequestMapping("/func/point")
public class SysPointController extends BaseController {

    @Autowired
    private ISysPointService sysPointService;

    @ApiOperation("获取点位列表")
    @GetMapping("/list")
    public TableDataInfo list(SysPoint point) {
        startPage();
        List<SysPoint> list = sysPointService.selectList(point);
        return getDataTable(list);
    }

    @ApiOperation("根据ID获取点位详情")
    @GetMapping("/{pointId}")
    public AjaxResult getById(@PathVariable Long pointId) {
        return success(sysPointService.selectById(pointId));
    }

    @ApiOperation("新增点位")
    @PostMapping
    public AjaxResult add(@RequestBody SysPoint point) {
        return toAjax(sysPointService.insert(point));
    }

    @ApiOperation("修改点位")
    @PutMapping
    public AjaxResult edit(@RequestBody SysPoint point) {
        return toAjax(sysPointService.update(point));
    }

    @ApiOperation("删除点位")
    @DeleteMapping("/{pointId}")
    public AjaxResult remove(@PathVariable Long pointId) {
        return toAjax(sysPointService.deleteById(pointId));
    }

    @ApiOperation("批量删除点位")
    @DeleteMapping("/batch")
    public AjaxResult batchRemove(@RequestBody Long[] pointIds) {
        return toAjax(sysPointService.deleteByIds(pointIds));
    }
}