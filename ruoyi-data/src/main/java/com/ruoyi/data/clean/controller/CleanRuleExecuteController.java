package com.ruoyi.data.clean.controller;

import com.ruoyi.data.clean.service.ICleanRuleExecuteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/clean/execute")
public class CleanRuleExecuteController {

    @Autowired
    private ICleanRuleExecuteService executeService;

    /**
     * 立即执行清洗任务
     */
    @PostMapping("/{id}")
    public String execute(@PathVariable("id") Long id) {

        executeService.executeTask(id);

        return "执行成功";
    }
}