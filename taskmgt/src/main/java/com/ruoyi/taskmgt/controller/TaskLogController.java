package com.ruoyi.taskmgt.controller;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.taskmgt.domain.bo.TaskLog;
import com.ruoyi.taskmgt.service.ITaskLogService;
import com.ruoyi.taskmgt.service.vo.TaskLogVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "任务日志管理")
@RestController
@RequestMapping("/taskmgt/logs")
@Slf4j
@RequiredArgsConstructor
public class TaskLogController extends BaseController {
    @Autowired
    private final ITaskLogService taskLogService;

    @ApiOperation("分页查询任务日志")
    @GetMapping
    public TableDataInfo list(@RequestParam(required = false)Long taskId,@RequestParam(required = false)Long stepId, @RequestParam(required = false)String eventType,@RequestParam(required = false)String operator,
                              @RequestParam(required = false)String beginTime,@RequestParam(required = false)String endTime,@RequestParam(required = false,defaultValue = "1")Integer pageNum, @RequestParam(required = false,defaultValue = "10")Integer pageSize,
                              @RequestParam(required = false,defaultValue = "createTime DESC")String displayOrder) {
        startPage(pageNum,pageSize,displayOrder);
        TaskLog query = TaskLog.builder().taskId(taskId).stepId(stepId).eventType(eventType).operator(operator).build();
        List<TaskLogVo> result = taskLogService.queryLogs(query,beginTime,endTime);
        return getDataTable(result);
    }

    @ApiOperation("获取日志详情")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id) {
        TaskLogVo vo = taskLogService.getLog(id);
        return success(vo);
    }

    @ApiOperation("根据任务ID查询日志")
    @GetMapping("/tasks")
    public TableDataInfo listByTask(@RequestParam Long taskId,@RequestParam(required = false,defaultValue = "createTime DESC")String displayOrder) {
        startPage();
        TaskLog query = new TaskLog();
        query.setTaskId(taskId);
        List<TaskLogVo> result = taskLogService.queryLogs(query,null,null);
        return getDataTable(result);
    }
}