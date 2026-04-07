package com.ruoyi.function.controller;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.function.controller.dto.request.PointCreateRequest;
import com.ruoyi.function.controller.dto.request.PointUpdateRequest;
import com.ruoyi.function.controller.dto.response.PointResponse;
import com.ruoyi.function.domain.SysPoint;
import com.ruoyi.function.enums.MapStatusEnum;
import com.ruoyi.function.enums.PointTypeEnum;
import com.ruoyi.function.exception.FunctionException;
import com.ruoyi.function.service.ISysPointService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
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
        SysPoint point = sysPointService.selectById(pointId);
        if (point == null) {
            throw FunctionException.pointNotFound(pointId);
        }

        // 转换为响应DTO
        PointResponse response = new PointResponse();
        BeanUtils.copyProperties(point, response);

        // 补充类型文本
        PointTypeEnum typeEnum = PointTypeEnum.getByCode(point.getPointType());
        response.setPointTypeText(typeEnum.getInfo());

        // 补充状态文本
        if (MapStatusEnum.ENABLED.getCode().equals(point.getStatus())) {
            response.setStatusText(MapStatusEnum.ENABLED.getInfo());
        } else {
            response.setStatusText(MapStatusEnum.DISABLED.getInfo());
        }

        return success(response);
    }

    @ApiOperation("新增点位")
    @PostMapping
    public AjaxResult add(@Valid @RequestBody PointCreateRequest request) {
        SysPoint point = new SysPoint();
        BeanUtils.copyProperties(request, point);
        return toAjax(sysPointService.insert(point));
    }

    @ApiOperation("修改点位")
    @PutMapping
    public AjaxResult edit(@Valid @RequestBody PointUpdateRequest request) {
        // 检查点位是否存在
        SysPoint existing = sysPointService.selectById(request.getPointId());
        if (existing == null) {
            throw FunctionException.pointNotFound(request.getPointId());
        }

        SysPoint point = new SysPoint();
        BeanUtils.copyProperties(request, point);
        return toAjax(sysPointService.update(point));
    }

    @ApiOperation("删除点位")
    @DeleteMapping("/{pointId}")
    public AjaxResult remove(@PathVariable Long pointId) {
        SysPoint point = sysPointService.selectById(pointId);
        if (point == null) {
            throw FunctionException.pointNotFound(pointId);
        }
        return toAjax(sysPointService.deleteById(pointId));
    }

    @ApiOperation("批量删除点位")
    @DeleteMapping("/batch")
    public AjaxResult batchRemove(@RequestBody Long[] pointIds) {
        if (pointIds == null || pointIds.length == 0) {
            return error("请选择要删除的点位");
        }
        return toAjax(sysPointService.deleteByIds(pointIds));
    }
}