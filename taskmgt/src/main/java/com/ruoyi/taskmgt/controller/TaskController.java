package com.ruoyi.taskmgt.controller;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.CloneFactory;
import com.ruoyi.common.validation.NewGroup;
import com.ruoyi.taskmgt.controller.dto.TaskDto;
import com.ruoyi.taskmgt.domain.bo.Task;
import com.ruoyi.taskmgt.service.ITaskService;
import com.ruoyi.taskmgt.service.vo.TaskAbnormalVo;
import com.ruoyi.taskmgt.service.vo.TaskVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "任务管理")
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/taskmgt")
public class TaskController extends BaseController {
    @Autowired
    private final ITaskService taskService;

    @ApiOperation("获取任务列表")
    @Log(title = "查看任务列表")
    @GetMapping("tasks")
    public TableDataInfo retrieveTasks(@RequestParam(required = false) Byte status, @RequestParam(required = false) Integer isGroupTask, @RequestParam(required = false) String name,
                                       @RequestParam(required = false) Long robotId, @RequestParam(required = false)Long robotGroupId, @RequestParam(required = false)Integer taskType,
                                       @RequestParam(required = false) Integer riskLevel, @RequestParam(required = false)Long templateId,
                                       @RequestParam(required = false,defaultValue = "1")Integer pageNum, @RequestParam(required = false,defaultValue = "10")Integer pageSize,
                                       @RequestParam(required = false,defaultValue = "status ASC, pending_order ASC, priority DESC, create_time DESC")String displayOrder)
    {
        startPage(pageNum,pageSize,displayOrder);
        List<TaskVo> result = this.taskService.retrieveTasks(status,isGroupTask,name,robotId,robotGroupId,taskType, riskLevel, templateId);
        return getDataTable(result);
    }

    @ApiOperation("创建任务")
    @Log(title = "创建任务", businessType = BusinessType.INSERT)
    @PostMapping("taskcore")
    public AjaxResult createTask(@Validated(value = NewGroup.class) @RequestBody TaskDto dto)
    {
        Task task = CloneFactory.copy(new Task(),dto);
        TaskVo result = this.taskService.createTask(task);
        return success(result);
    }
    @ApiOperation("修改任务")
    @Log(title = "修改任务", businessType = BusinessType.UPDATE)
    @PutMapping("tasks/{id}")
    public AjaxResult updateTask(@PathVariable Long id,@Validated @RequestBody TaskDto dto)
    {
        Task task = CloneFactory.copy(new Task(),dto);
        task.setId(id);
        this.taskService.updateTask(task);
        return success();
    }
    @ApiOperation("删除任务")
    @Log(title = "删除任务", businessType = BusinessType.DELETE)
    @DeleteMapping("tasks/{id}")
    public AjaxResult deleteTask(@PathVariable Long id)
    {
        this.taskService.deleteTask(id);
        return success();
    }
    @ApiOperation("查看任务详情")
    @Log(title = "查看任务详情")
    @GetMapping("tasks/{id}")
    public AjaxResult getTask(@PathVariable Long id)
    {
        TaskVo result = this.taskService.getTask(id);
        return success(result);
    }
    @ApiOperation("禁用任务")
    @Log(title = "禁用任务" ,businessType = BusinessType.UPDATE)
    @PutMapping("tasks/{id}/ban")
    public AjaxResult banTask(@PathVariable Long id)
    {
        this.taskService.banTask(id);
        return success();
    }
    @ApiOperation("恢复任务")
    @Log(title = "恢复任务" ,businessType = BusinessType.UPDATE)
    @PutMapping("tasks/{id}/resume")
    public AjaxResult resumeTask(@PathVariable Long id)
    {
        this.taskService.resumeTask(id);
        return success();
    }
    @ApiOperation("暂停任务")
    @Log(title = "暂停任务" ,businessType = BusinessType.UPDATE)
    @PutMapping("tasks/{id}/pause")
    public AjaxResult pauseTask(@PathVariable Long id)
    {
        this.taskService.pauseTask(id);
        return success();
    }
    @ApiOperation("继续任务")
    @Log(title = "继续任务" ,businessType = BusinessType.UPDATE)
    @PutMapping("tasks/{id}/continue")
    public AjaxResult continueTask(@PathVariable Long id)
    {
        this.taskService.continueTask(id);
        return success();
    }
    @ApiOperation("停止任务")
    @Log(title = "停止任务" ,businessType = BusinessType.UPDATE)
    @PutMapping("tasks/{id}/terminate")
    public AjaxResult terminateTask(@PathVariable Long id, @Validated @RequestBody TaskDto dto)
    {
        this.taskService.terminateTask(id,dto.getTerminateReason());
        return success();
    }
    @ApiOperation("取消任务")
    @Log(title = "取消任务" ,businessType = BusinessType.UPDATE)
    @PutMapping("tasks/{id}/cancel")
    public AjaxResult cancelTask(@PathVariable Long id)
    {
        this.taskService.cancelTask(id);
        return success();
    }

    @ApiOperation("获取异常任务列表")
    @GetMapping("/tasks/abnormal")
    public TableDataInfo listAbnormalTasks(
            @RequestParam(required = false) Integer riskLevel,
            @RequestParam(required = false) Long robotId,
            @RequestParam(required = false) Long robotGroupId,
            @RequestParam(required = false,defaultValue = "1") Integer pageNum,
            @RequestParam(required = false,defaultValue = "10") Integer pageSize) {
        startPage(pageNum, pageSize);
        List<TaskAbnormalVo> list = taskService.getAbnormalTasks(riskLevel, robotId, robotGroupId);
        return getDataTable(list);
    }

    @ApiOperation("解决任务风险")
    @Log(title = "解决任务风险", businessType = BusinessType.UPDATE)
    @PutMapping("/tasks/{id}/resolve")
    public AjaxResult resolveRisk(@PathVariable Long id) {
        taskService.resolveRisk(id);
        return success();
    }
}

