package com.ruoyi.qa.ChatManage.controller;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.qa.ChatManage.domain.QaRobotChatRel;
import com.ruoyi.qa.ChatManage.service.IQaRobotChatRelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/qa/chat/rel")
public class QaRobotChatRelController extends BaseController
{
    @Autowired
    private IQaRobotChatRelService qaRobotChatRelService;

    @GetMapping("/list")
    public TableDataInfo list(QaRobotChatRel qaRobotChatRel)
    {
        startPage();
        List<QaRobotChatRel> list = qaRobotChatRelService.selectQaRobotChatRelList(qaRobotChatRel);
        return getDataTable(list);
    }

    @GetMapping("/{robotId}")
    public AjaxResult getInfo(@PathVariable("robotId") Long robotId)
    {
        return success(qaRobotChatRelService.selectQaRobotChatRelByRobotId(robotId));
    }

    @PostMapping
    public AjaxResult add(@RequestBody QaRobotChatRel qaRobotChatRel)
    {
        return toAjax(qaRobotChatRelService.insertQaRobotChatRel(qaRobotChatRel));
    }

    @PutMapping
    public AjaxResult edit(@RequestBody QaRobotChatRel qaRobotChatRel)
    {
        return toAjax(qaRobotChatRelService.updateQaRobotChatRel(qaRobotChatRel));
    }

    @DeleteMapping("/{robotIds}")
    public AjaxResult remove(@PathVariable Long[] robotIds)
    {
        return toAjax(qaRobotChatRelService.deleteQaRobotChatRelByRobotIds(robotIds));
    }
}
