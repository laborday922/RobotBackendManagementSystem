package com.ruoyi.qa.KnowledgeBase.controller;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.qa.KnowledgeBase.domain.QaKnowledgeBase;
import com.ruoyi.qa.KnowledgeBase.domain.vo.QaKnowledgeBaseDetailVo;
import com.ruoyi.qa.KnowledgeBase.domain.vo.QaKnowledgeBaseListVo;
import com.ruoyi.qa.KnowledgeBase.service.IQaKnowledgeBaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/qa/knowledgeBase")
public class QaKnowledgeBaseController extends BaseController
{
    @Autowired
    private IQaKnowledgeBaseService qaKnowledgeBaseService;

    @GetMapping("/list")
    public TableDataInfo list(QaKnowledgeBase qaKnowledgeBase)
    {
        startPage();
        List<QaKnowledgeBase> list = qaKnowledgeBaseService.selectQaKnowledgeBaseList(qaKnowledgeBase);
        List<QaKnowledgeBaseListVo> voList = list.stream().map(this::toListVo).collect(Collectors.toList());
        TableDataInfo dataTable = getDataTable(list);
        dataTable.setRows(voList);
        return dataTable;
    }

    @GetMapping("/options")
    public AjaxResult options()
    {
        return success(qaKnowledgeBaseService.selectQaKnowledgeBaseOptions());
    }

    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(toDetailVo(qaKnowledgeBaseService.selectQaKnowledgeBaseById(id)));
    }

    @PostMapping
    public AjaxResult add(@RequestBody QaKnowledgeBase qaKnowledgeBase)
    {
        return toAjax(qaKnowledgeBaseService.insertQaKnowledgeBase(qaKnowledgeBase));
    }

    @PutMapping
    public AjaxResult edit(@RequestBody QaKnowledgeBase qaKnowledgeBase)
    {
        return toAjax(qaKnowledgeBaseService.updateQaKnowledgeBase(qaKnowledgeBase));
    }

    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(qaKnowledgeBaseService.deleteQaKnowledgeBaseByIds(ids));
    }

    private QaKnowledgeBaseListVo toListVo(QaKnowledgeBase qaKnowledgeBase)
    {
        if (qaKnowledgeBase == null)
        {
            return null;
        }
        QaKnowledgeBaseListVo vo = new QaKnowledgeBaseListVo();
        vo.setId(qaKnowledgeBase.getId());
        vo.setKbName(qaKnowledgeBase.getKbName());
        vo.setKbDesc(qaKnowledgeBase.getKbDesc());
        vo.setDifyEnabled(qaKnowledgeBase.getDifyEnabled());
        vo.setDifyDatasetId(qaKnowledgeBase.getDifyDatasetId());
        vo.setHasDifyDatasetApiKey(StringUtils.hasText(qaKnowledgeBase.getDifyDatasetApiKey()));
        vo.setDifyDatasetApiKeyMasked(maskSecret(qaKnowledgeBase.getDifyDatasetApiKey()));
        vo.setDifyIndexingTechnique(qaKnowledgeBase.getDifyIndexingTechnique());
        vo.setDifyDocForm(qaKnowledgeBase.getDifyDocForm());
        vo.setDifyDocLanguage(qaKnowledgeBase.getDifyDocLanguage());
        vo.setDifyProcessRuleMode(qaKnowledgeBase.getDifyProcessRuleMode());
        vo.setDifyRuleSeparator(qaKnowledgeBase.getDifyRuleSeparator());
        vo.setDifyRuleMaxTokens(qaKnowledgeBase.getDifyRuleMaxTokens());
        vo.setDifyRuleChunkOverlap(qaKnowledgeBase.getDifyRuleChunkOverlap());
        vo.setApiEnabled(qaKnowledgeBase.getApiEnabled());
        vo.setApiBaseUrl(qaKnowledgeBase.getApiBaseUrl());
        vo.setHasApiAuthToken(StringUtils.hasText(qaKnowledgeBase.getApiAuthToken()));
        vo.setApiAuthTokenMasked(maskSecret(qaKnowledgeBase.getApiAuthToken()));
        vo.setFileCount(qaKnowledgeBase.getFileCount());
        vo.setUpdateTime(qaKnowledgeBase.getUpdateTime());
        return vo;
    }

    private QaKnowledgeBaseDetailVo toDetailVo(QaKnowledgeBase qaKnowledgeBase)
    {
        if (qaKnowledgeBase == null)
        {
            return null;
        }
        QaKnowledgeBaseDetailVo vo = new QaKnowledgeBaseDetailVo();
        vo.setId(qaKnowledgeBase.getId());
        vo.setKbName(qaKnowledgeBase.getKbName());
        vo.setKbDesc(qaKnowledgeBase.getKbDesc());
        vo.setDifyEnabled(qaKnowledgeBase.getDifyEnabled());
        vo.setDifyDatasetId(qaKnowledgeBase.getDifyDatasetId());
        vo.setHasDifyDatasetApiKey(StringUtils.hasText(qaKnowledgeBase.getDifyDatasetApiKey()));
        vo.setDifyIndexingTechnique(qaKnowledgeBase.getDifyIndexingTechnique());
        vo.setDifyDocForm(qaKnowledgeBase.getDifyDocForm());
        vo.setDifyDocLanguage(qaKnowledgeBase.getDifyDocLanguage());
        vo.setDifyProcessRuleMode(qaKnowledgeBase.getDifyProcessRuleMode());
        vo.setDifyRuleSeparator(qaKnowledgeBase.getDifyRuleSeparator());
        vo.setDifyRuleMaxTokens(qaKnowledgeBase.getDifyRuleMaxTokens());
        vo.setDifyRuleChunkOverlap(qaKnowledgeBase.getDifyRuleChunkOverlap());
        vo.setDifyRemoveExtraSpaces(qaKnowledgeBase.getDifyRemoveExtraSpaces());
        vo.setDifyRemoveUrlsEmails(qaKnowledgeBase.getDifyRemoveUrlsEmails());
        vo.setApiEnabled(qaKnowledgeBase.getApiEnabled());
        vo.setApiBaseUrl(qaKnowledgeBase.getApiBaseUrl());
        vo.setHasApiAuthToken(StringUtils.hasText(qaKnowledgeBase.getApiAuthToken()));
        vo.setFileCount(qaKnowledgeBase.getFileCount());
        return vo;
    }

    private String maskSecret(String value)
    {
        if (!StringUtils.hasText(value))
        {
            return "未配置";
        }
        String text = value.trim();
        if (text.length() <= 10)
        {
            return "已配置";
        }
        return text.substring(0, 6) + "******" + text.substring(text.length() - 4);
    }
}
