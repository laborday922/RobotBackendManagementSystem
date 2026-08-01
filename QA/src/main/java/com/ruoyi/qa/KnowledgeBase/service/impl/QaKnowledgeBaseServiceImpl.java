package com.ruoyi.qa.KnowledgeBase.service.impl;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.qa.KnowledgeBase.domain.QaKnowledgeBase;
import com.ruoyi.qa.KnowledgeBase.mapper.QaKnowledgeBaseMapper;
import com.ruoyi.qa.KnowledgeBase.service.IQaKnowledgeBaseService;
import com.ruoyi.qa.QAFile.service.IQaFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class QaKnowledgeBaseServiceImpl implements IQaKnowledgeBaseService
{
    private static final String DEFAULT_DIFY_INDEXING_TECHNIQUE = "high_quality";
    private static final String DEFAULT_DIFY_DOC_FORM = "qa_model";
    private static final String DEFAULT_DIFY_DOC_LANGUAGE = "Chinese Simplified";
    private static final String DEFAULT_DIFY_PROCESS_RULE_MODE = "custom";
    private static final String DEFAULT_DIFY_RULE_SEPARATOR = "\n\n";
    private static final int DEFAULT_DIFY_RULE_MAX_TOKENS = 500;
    private static final int DEFAULT_DIFY_RULE_CHUNK_OVERLAP = 50;

    @Autowired
    private QaKnowledgeBaseMapper qaKnowledgeBaseMapper;

    @Autowired
    private IQaFileService qaFileService;

    @Override
    public QaKnowledgeBase selectQaKnowledgeBaseById(Long id)
    {
        return qaKnowledgeBaseMapper.selectQaKnowledgeBaseById(id);
    }

    @Override
    public List<QaKnowledgeBase> selectQaKnowledgeBaseList(QaKnowledgeBase qaKnowledgeBase)
    {
        return qaKnowledgeBaseMapper.selectQaKnowledgeBaseList(qaKnowledgeBase);
    }

    @Override
    public List<QaKnowledgeBase> selectQaKnowledgeBaseOptions()
    {
        return qaKnowledgeBaseMapper.selectQaKnowledgeBaseOptions();
    }

    @Override
    public int insertQaKnowledgeBase(QaKnowledgeBase qaKnowledgeBase)
    {
        normalizeAndValidateForCreate(qaKnowledgeBase);
        ensureNameUnique(qaKnowledgeBase.getKbName(), null);
        return qaKnowledgeBaseMapper.insertQaKnowledgeBase(qaKnowledgeBase);
    }

    @Override
    public int updateQaKnowledgeBase(QaKnowledgeBase qaKnowledgeBase)
    {
        if (qaKnowledgeBase == null || qaKnowledgeBase.getId() == null)
        {
            throw new ServiceException("知识库ID不能为空");
        }
        QaKnowledgeBase db = qaKnowledgeBaseMapper.selectQaKnowledgeBaseById(qaKnowledgeBase.getId());
        if (db == null)
        {
            throw new ServiceException("知识库不存在");
        }

        if (!StringUtils.hasText(qaKnowledgeBase.getKbName()))
        {
            throw new ServiceException("知识库名称不能为空");
        }
        qaKnowledgeBase.setKbName(qaKnowledgeBase.getKbName().trim());
        if (qaKnowledgeBase.getKbDesc() != null)
        {
            qaKnowledgeBase.setKbDesc(qaKnowledgeBase.getKbDesc().trim());
        }
        ensureNameUnique(qaKnowledgeBase.getKbName(), qaKnowledgeBase.getId());

        // 仅允许修改名称和描述，其余配置保持创建时的值不变
        qaKnowledgeBase.setDifyEnabled(db.getDifyEnabled());
        qaKnowledgeBase.setDifyDatasetId(db.getDifyDatasetId());
        qaKnowledgeBase.setDifyDatasetApiKey(db.getDifyDatasetApiKey());
        qaKnowledgeBase.setDifyIndexingTechnique(db.getDifyIndexingTechnique());
        qaKnowledgeBase.setDifyDocForm(db.getDifyDocForm());
        qaKnowledgeBase.setDifyDocLanguage(db.getDifyDocLanguage());
        qaKnowledgeBase.setDifyProcessRuleMode(db.getDifyProcessRuleMode());
        qaKnowledgeBase.setDifyRuleSeparator(db.getDifyRuleSeparator());
        qaKnowledgeBase.setDifyRuleMaxTokens(db.getDifyRuleMaxTokens());
        qaKnowledgeBase.setDifyRuleChunkOverlap(db.getDifyRuleChunkOverlap());
        qaKnowledgeBase.setDifyRemoveExtraSpaces(db.getDifyRemoveExtraSpaces());
        qaKnowledgeBase.setDifyRemoveUrlsEmails(db.getDifyRemoveUrlsEmails());
        qaKnowledgeBase.setApiEnabled(db.getApiEnabled());
        qaKnowledgeBase.setApiBaseUrl(db.getApiBaseUrl());
        qaKnowledgeBase.setApiAuthToken(db.getApiAuthToken());
        return qaKnowledgeBaseMapper.updateQaKnowledgeBase(qaKnowledgeBase);
    }

    @Override
    public int deleteQaKnowledgeBaseByIds(Long[] ids)
    {
        if (ids == null || ids.length == 0)
        {
            return 0;
        }
        qaFileService.deleteQaFileByKnowledgeBaseIds(ids);
        return qaKnowledgeBaseMapper.deleteQaKnowledgeBaseByIds(ids);
    }

    private void normalizeAndValidateForCreate(QaKnowledgeBase qaKnowledgeBase)
    {
        if (qaKnowledgeBase == null)
        {
            throw new ServiceException("知识库配置不能为空");
        }
        if (!StringUtils.hasText(qaKnowledgeBase.getKbName()))
        {
            throw new ServiceException("知识库名称不能为空");
        }
        qaKnowledgeBase.setKbName(qaKnowledgeBase.getKbName().trim());
        if (qaKnowledgeBase.getKbDesc() != null)
        {
            qaKnowledgeBase.setKbDesc(qaKnowledgeBase.getKbDesc().trim());
        }

        boolean difyEnabled = Boolean.TRUE.equals(qaKnowledgeBase.getDifyEnabled());
        qaKnowledgeBase.setDifyEnabled(difyEnabled);
        if (difyEnabled)
        {
            if (!StringUtils.hasText(qaKnowledgeBase.getDifyDatasetId()))
            {
                throw new ServiceException("启用Dify知识库时，Dify知识库ID不能为空");
            }
            if (!StringUtils.hasText(qaKnowledgeBase.getDifyDatasetApiKey()))
            {
                throw new ServiceException("启用Dify知识库时，Dataset API Key不能为空");
            }
            qaKnowledgeBase.setDifyDatasetId(qaKnowledgeBase.getDifyDatasetId().trim());
            qaKnowledgeBase.setDifyDatasetApiKey(qaKnowledgeBase.getDifyDatasetApiKey().trim());
            qaKnowledgeBase.setDifyIndexingTechnique(defaultIfBlank(qaKnowledgeBase.getDifyIndexingTechnique(), DEFAULT_DIFY_INDEXING_TECHNIQUE));
            qaKnowledgeBase.setDifyDocForm(defaultIfBlank(qaKnowledgeBase.getDifyDocForm(), DEFAULT_DIFY_DOC_FORM));
            qaKnowledgeBase.setDifyDocLanguage(defaultIfBlank(qaKnowledgeBase.getDifyDocLanguage(), DEFAULT_DIFY_DOC_LANGUAGE));
            qaKnowledgeBase.setDifyProcessRuleMode(defaultIfBlank(qaKnowledgeBase.getDifyProcessRuleMode(), DEFAULT_DIFY_PROCESS_RULE_MODE));
            qaKnowledgeBase.setDifyRuleSeparator(qaKnowledgeBase.getDifyRuleSeparator() == null ? DEFAULT_DIFY_RULE_SEPARATOR : qaKnowledgeBase.getDifyRuleSeparator());
            qaKnowledgeBase.setDifyRuleMaxTokens(qaKnowledgeBase.getDifyRuleMaxTokens() == null ? DEFAULT_DIFY_RULE_MAX_TOKENS : qaKnowledgeBase.getDifyRuleMaxTokens());
            qaKnowledgeBase.setDifyRuleChunkOverlap(qaKnowledgeBase.getDifyRuleChunkOverlap() == null ? DEFAULT_DIFY_RULE_CHUNK_OVERLAP : qaKnowledgeBase.getDifyRuleChunkOverlap());
            qaKnowledgeBase.setDifyRemoveExtraSpaces(qaKnowledgeBase.getDifyRemoveExtraSpaces() == null || qaKnowledgeBase.getDifyRemoveExtraSpaces());
            qaKnowledgeBase.setDifyRemoveUrlsEmails(Boolean.TRUE.equals(qaKnowledgeBase.getDifyRemoveUrlsEmails()));
        }
        else
        {
            qaKnowledgeBase.setDifyDatasetId(null);
            qaKnowledgeBase.setDifyDatasetApiKey(null);
            qaKnowledgeBase.setDifyIndexingTechnique(null);
            qaKnowledgeBase.setDifyDocForm(null);
            qaKnowledgeBase.setDifyDocLanguage(null);
            qaKnowledgeBase.setDifyProcessRuleMode(null);
            qaKnowledgeBase.setDifyRuleSeparator(null);
            qaKnowledgeBase.setDifyRuleMaxTokens(null);
            qaKnowledgeBase.setDifyRuleChunkOverlap(null);
            qaKnowledgeBase.setDifyRemoveExtraSpaces(null);
            qaKnowledgeBase.setDifyRemoveUrlsEmails(null);
        }

        boolean apiEnabled = Boolean.TRUE.equals(qaKnowledgeBase.getApiEnabled());
        qaKnowledgeBase.setApiEnabled(apiEnabled);
        if (apiEnabled)
        {
            if (!StringUtils.hasText(qaKnowledgeBase.getApiBaseUrl()))
            {
                throw new ServiceException("启用外部API时，API地址不能为空");
            }
            qaKnowledgeBase.setApiBaseUrl(qaKnowledgeBase.getApiBaseUrl().trim());
            if (qaKnowledgeBase.getApiAuthToken() != null)
            {
                qaKnowledgeBase.setApiAuthToken(qaKnowledgeBase.getApiAuthToken().trim());
            }
        }
        else
        {
            qaKnowledgeBase.setApiBaseUrl(null);
            qaKnowledgeBase.setApiAuthToken(null);
        }
    }

    private void ensureNameUnique(String kbName, Long excludeId)
    {
        QaKnowledgeBase existed = qaKnowledgeBaseMapper.selectQaKnowledgeBaseByName(kbName);
        if (existed == null)
        {
            return;
        }
        if (excludeId != null && excludeId.equals(existed.getId()))
        {
            return;
        }
        throw new ServiceException("知识库名称已存在");
    }

    private String defaultIfBlank(String value, String defaultValue)
    {
        return StringUtils.hasText(value) ? value.trim() : defaultValue;
    }
}
