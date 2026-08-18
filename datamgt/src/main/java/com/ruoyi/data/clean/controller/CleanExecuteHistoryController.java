package com.ruoyi.data.clean.controller;

import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.data.clean.domain.CleanExecuteHistory;
import com.ruoyi.data.clean.service.CleanExecuteHistoryService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/clean/history")
public class CleanExecuteHistoryController {

    @Resource
    private CleanExecuteHistoryService service;

    /**
     * 新增执行记录
     */
    @PostMapping("/create")
    public AjaxResult create(@RequestBody CleanExecuteHistory history) {
        try {
            Long id = service.createRecord(history);
            return AjaxResult.success(id);
        } catch (Exception e) {
            return AjaxResult.error("新增记录失败：" + e.getMessage());
        }
    }

    /**
     * 查询详情
     */
    @GetMapping("/{id}")
    public AjaxResult getById(@PathVariable Long id) {
        try {
            CleanExecuteHistory history = service.getById(id);
            return AjaxResult.success(history);
        } catch (Exception e) {
            return AjaxResult.error("查询详情失败：" + e.getMessage());
        }
    }

    /**
     * 查询全部
     */
    @GetMapping("/list")
    public AjaxResult listAll() {
        try {
            List<CleanExecuteHistory> list = service.listAll();
            return AjaxResult.success(list);
        } catch (Exception e) {
            return AjaxResult.error("查询列表失败：" + e.getMessage());
        }
    }

    /**
     * 编辑定时任务
     */
    @PutMapping("/{id}")
    public AjaxResult update(@PathVariable Long id,
                             @RequestBody CleanExecuteHistory history) {
        try {
            history.setId(id);
            service.update(history);
            return AjaxResult.success("更新成功");
        } catch (Exception e) {
            return AjaxResult.error("更新失败：" + e.getMessage());
        }
    }

    /**
     * 删除
     */
    @DeleteMapping("/{id}")
    public AjaxResult delete(@PathVariable Long id) {
        try {
            service.delete(id);
            return AjaxResult.success("删除成功");
        } catch (Exception e) {
            return AjaxResult.error("删除失败：" + e.getMessage());
        }
    }
}