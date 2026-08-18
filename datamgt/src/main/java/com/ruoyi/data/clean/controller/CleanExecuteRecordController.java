package com.ruoyi.data.clean.controller;

import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.data.clean.domain.CleanExecuteRecord;
import com.ruoyi.data.clean.service.CleanExecuteRecordService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/clean/record")
public class CleanExecuteRecordController {

    @Resource
    private CleanExecuteRecordService service;

    /**
     * 查询执行记录列表（定时 + 手动）
     */
    @GetMapping("/list")
    public AjaxResult listAll() {
        try {
            List<CleanExecuteRecord> list = service.listAll();
            return AjaxResult.success(list);
        } catch (Exception e) {
            return AjaxResult.error("查询执行记录失败：" + e.getMessage());
        }
    }
}
