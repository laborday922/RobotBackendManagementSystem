package com.ruoyi.qa.KnowledgeBase.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class QaKnowledgeBase extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long id;

    @Excel(name = "知识库名称")
    private String kbName;

    @Excel(name = "知识库描述")
    private String kbDesc;

    @Excel(name = "启用Dify知识库")
    private Boolean difyEnabled;

    @Excel(name = "Dify知识库ID")
    private String difyDatasetId;

    @Excel(name = "Dify知识库API Key")
    private String difyDatasetApiKey;

    @Excel(name = "Dify索引方式")
    private String difyIndexingTechnique;

    @Excel(name = "Dify文档模式")
    private String difyDocForm;

    @Excel(name = "Dify文档语言")
    private String difyDocLanguage;

    @Excel(name = "Dify处理模式")
    private String difyProcessRuleMode;

    @Excel(name = "Dify分段分隔符")
    private String difyRuleSeparator;

    @Excel(name = "Dify分段最大长度")
    private Integer difyRuleMaxTokens;

    @Excel(name = "Dify分段重叠长度")
    private Integer difyRuleChunkOverlap;

    @Excel(name = "Dify去除多余空格")
    private Boolean difyRemoveExtraSpaces;

    @Excel(name = "Dify去除URL和邮箱")
    private Boolean difyRemoveUrlsEmails;

    @Excel(name = "启用API")
    private Boolean apiEnabled;

    @Excel(name = "API地址")
    private String apiBaseUrl;

    @Excel(name = "API Token")
    private String apiAuthToken;

    private Long fileCount;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getKbName()
    {
        return kbName;
    }

    public void setKbName(String kbName)
    {
        this.kbName = kbName;
    }

    public String getKbDesc()
    {
        return kbDesc;
    }

    public void setKbDesc(String kbDesc)
    {
        this.kbDesc = kbDesc;
    }

    public Boolean getDifyEnabled()
    {
        return difyEnabled;
    }

    public void setDifyEnabled(Boolean difyEnabled)
    {
        this.difyEnabled = difyEnabled;
    }

    public String getDifyDatasetId()
    {
        return difyDatasetId;
    }

    public void setDifyDatasetId(String difyDatasetId)
    {
        this.difyDatasetId = difyDatasetId;
    }

    public String getDifyDatasetApiKey()
    {
        return difyDatasetApiKey;
    }

    public void setDifyDatasetApiKey(String difyDatasetApiKey)
    {
        this.difyDatasetApiKey = difyDatasetApiKey;
    }

    public String getDifyIndexingTechnique()
    {
        return difyIndexingTechnique;
    }

    public void setDifyIndexingTechnique(String difyIndexingTechnique)
    {
        this.difyIndexingTechnique = difyIndexingTechnique;
    }

    public String getDifyDocForm()
    {
        return difyDocForm;
    }

    public void setDifyDocForm(String difyDocForm)
    {
        this.difyDocForm = difyDocForm;
    }

    public String getDifyDocLanguage()
    {
        return difyDocLanguage;
    }

    public void setDifyDocLanguage(String difyDocLanguage)
    {
        this.difyDocLanguage = difyDocLanguage;
    }

    public String getDifyProcessRuleMode()
    {
        return difyProcessRuleMode;
    }

    public void setDifyProcessRuleMode(String difyProcessRuleMode)
    {
        this.difyProcessRuleMode = difyProcessRuleMode;
    }

    public String getDifyRuleSeparator()
    {
        return difyRuleSeparator;
    }

    public void setDifyRuleSeparator(String difyRuleSeparator)
    {
        this.difyRuleSeparator = difyRuleSeparator;
    }

    public Integer getDifyRuleMaxTokens()
    {
        return difyRuleMaxTokens;
    }

    public void setDifyRuleMaxTokens(Integer difyRuleMaxTokens)
    {
        this.difyRuleMaxTokens = difyRuleMaxTokens;
    }

    public Integer getDifyRuleChunkOverlap()
    {
        return difyRuleChunkOverlap;
    }

    public void setDifyRuleChunkOverlap(Integer difyRuleChunkOverlap)
    {
        this.difyRuleChunkOverlap = difyRuleChunkOverlap;
    }

    public Boolean getDifyRemoveExtraSpaces()
    {
        return difyRemoveExtraSpaces;
    }

    public void setDifyRemoveExtraSpaces(Boolean difyRemoveExtraSpaces)
    {
        this.difyRemoveExtraSpaces = difyRemoveExtraSpaces;
    }

    public Boolean getDifyRemoveUrlsEmails()
    {
        return difyRemoveUrlsEmails;
    }

    public void setDifyRemoveUrlsEmails(Boolean difyRemoveUrlsEmails)
    {
        this.difyRemoveUrlsEmails = difyRemoveUrlsEmails;
    }

    public Boolean getApiEnabled()
    {
        return apiEnabled;
    }

    public void setApiEnabled(Boolean apiEnabled)
    {
        this.apiEnabled = apiEnabled;
    }

    public String getApiBaseUrl()
    {
        return apiBaseUrl;
    }

    public void setApiBaseUrl(String apiBaseUrl)
    {
        this.apiBaseUrl = apiBaseUrl;
    }

    public String getApiAuthToken()
    {
        return apiAuthToken;
    }

    public void setApiAuthToken(String apiAuthToken)
    {
        this.apiAuthToken = apiAuthToken;
    }

    public Long getFileCount()
    {
        return fileCount;
    }

    public void setFileCount(Long fileCount)
    {
        this.fileCount = fileCount;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("kbName", getKbName())
            .append("kbDesc", getKbDesc())
            .append("difyEnabled", getDifyEnabled())
            .append("difyDatasetId", getDifyDatasetId())
            .append("difyDatasetApiKey", getDifyDatasetApiKey())
            .append("difyIndexingTechnique", getDifyIndexingTechnique())
            .append("difyDocForm", getDifyDocForm())
            .append("difyDocLanguage", getDifyDocLanguage())
            .append("difyProcessRuleMode", getDifyProcessRuleMode())
            .append("difyRuleSeparator", getDifyRuleSeparator())
            .append("difyRuleMaxTokens", getDifyRuleMaxTokens())
            .append("difyRuleChunkOverlap", getDifyRuleChunkOverlap())
            .append("difyRemoveExtraSpaces", getDifyRemoveExtraSpaces())
            .append("difyRemoveUrlsEmails", getDifyRemoveUrlsEmails())
            .append("apiEnabled", getApiEnabled())
            .append("apiBaseUrl", getApiBaseUrl())
            .append("apiAuthToken", getApiAuthToken())
            .append("fileCount", getFileCount())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
