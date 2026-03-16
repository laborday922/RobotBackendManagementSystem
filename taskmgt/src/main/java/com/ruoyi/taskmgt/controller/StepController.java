package com.ruoyi.taskmgt.controller;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.CloneFactory;
import com.ruoyi.common.validation.NewGroup;
import com.ruoyi.taskmgt.controller.dto.TaskStepDto;
import com.ruoyi.taskmgt.domain.bo.TaskStep;
import com.ruoyi.taskmgt.service.IStepService;
import com.ruoyi.taskmgt.service.vo.TaskStepVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Api(tags = "任务步骤管理")
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/taskmgt")
public class StepController extends BaseController {
    @Autowired
    private IStepService stepService;
    @ApiOperation("创建任务步骤")
    @Log(title = "创建步骤", businessType = BusinessType.INSERT)
    @PostMapping("tasks/{id}/steps")
    public AjaxResult createTaskSteps(@PathVariable Long id,@Validated(value = NewGroup.class) @RequestBody List<TaskStepDto> dtos)
    {
        List<TaskStep> steps = dtos.stream()
                .map(dto -> CloneFactory.copy(new TaskStep(), dto))
                .collect(Collectors.toList());
        List<TaskStepVo> result = this.stepService.createSteps(id,steps);
        return success(result);
    }

    @ApiOperation("修改任务步骤")
    @Log(title = "修改步骤", businessType = BusinessType.UPDATE)
    @PutMapping("tasks/{id}/steps")
    public AjaxResult updateTaskSteps(@PathVariable Long id,@Validated @RequestBody List<TaskStepDto> dtos)
    {
        List<TaskStep> steps = dtos.stream()
                .map(dto -> CloneFactory.copy(new TaskStep(), dto))
                .collect(Collectors.toList());
        this.stepService.updateSteps(id,steps);
        return success();
    }

    @ApiOperation("查看任务步骤")
    @Log(title = "查看步骤")
    @GetMapping("tasks/{id}/steps")
    public AjaxResult getTaskSteps(@PathVariable Long id)
    {
        List<TaskStepVo> result = this.stepService.retrieveSteps(id);
        return success(result);
    }

    @ApiOperation("完成步骤")
    @Log(title = "完成步骤", businessType = BusinessType.UPDATE)
    @PutMapping("steps/{id}/complete")
    public AjaxResult completeStep(@PathVariable Long id) {
        stepService.completeStep(id);
        return success();
    }
}
