package com.ruoyi.qa.ChatManage.controller;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.qa.ChatManage.domain.QaChat;
import com.ruoyi.qa.ChatManage.domain.vo.QaChatDetailVo;
import com.ruoyi.qa.ChatManage.domain.vo.QaChatListVo;
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
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

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
        List<QaChatListVo> voList = list.stream().map(this::toListVo).collect(Collectors.toList());
        TableDataInfo dataTable = getDataTable(list);
        dataTable.setRows(voList);
        return dataTable;
    }

    @GetMapping("/options")
    public AjaxResult options()
    {
        return success(qaChatService.selectQaChatOptions());
    }

    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(toDetailVo(qaChatService.selectQaChatById(id)));
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

    private QaChatListVo toListVo(QaChat qaChat)
    {
        if (qaChat == null)
        {
            return null;
        }
        QaChatListVo vo = new QaChatListVo();
        vo.setId(qaChat.getId());
        vo.setChatName(qaChat.getChatName());
        vo.setChatDesc(qaChat.getChatDesc());
        vo.setHasDifyApiKey(StringUtils.hasText(qaChat.getDifyApiKey()));
        vo.setApiKeyMasked(maskApiKey(qaChat.getDifyApiKey()));
        vo.setUpdateTime(qaChat.getUpdateTime());
        return vo;
    }

    private QaChatDetailVo toDetailVo(QaChat qaChat)
    {
        if (qaChat == null)
        {
            return null;
        }
        QaChatDetailVo vo = new QaChatDetailVo();
        vo.setId(qaChat.getId());
        vo.setChatName(qaChat.getChatName());
        vo.setChatDesc(qaChat.getChatDesc());
        vo.setDifyApiKey("");
        vo.setHasDifyApiKey(StringUtils.hasText(qaChat.getDifyApiKey()));
        return vo;
    }

    private String maskApiKey(String apiKey)
    {
        if (!StringUtils.hasText(apiKey))
        {
            return "未配置";
        }
        String value = apiKey.trim();
        if (value.length() <= 10)
        {
            return "已配置";
        }
        return value.substring(0, 6) + "******" + value.substring(value.length() - 4);
    }
}
