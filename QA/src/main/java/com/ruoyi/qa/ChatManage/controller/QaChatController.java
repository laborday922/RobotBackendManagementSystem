package com.ruoyi.qa.ChatManage.controller;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.qa.ChatManage.domain.QaChat;
import com.ruoyi.qa.ChatManage.service.IQaChatService;
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
@RequestMapping("/qa/chat/manage")
public class QaChatController extends BaseController
{
    @Autowired
    private IQaChatService qaChatService;

    @GetMapping("/list")
    public TableDataInfo list(QaChat qaChat)
    {
        startPage();
        List<QaChat> list = qaChatService.selectQaChatList(qaChat);
        return getDataTable(list);
    }

    @GetMapping("/options")
    public AjaxResult options()
    {
        return success(qaChatService.selectQaChatOptions());
    }

    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(qaChatService.selectQaChatById(id));
    }

    @PostMapping
    public AjaxResult add(@RequestBody QaChat qaChat)
    {
        return toAjax(qaChatService.insertQaChat(qaChat));
    }

    @PutMapping
    public AjaxResult edit(@RequestBody QaChat qaChat)
    {
        return toAjax(qaChatService.updateQaChat(qaChat));
    }

    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(qaChatService.deleteQaChatByIds(ids));
    }
}
