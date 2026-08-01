package com.ruoyi.qa.KnowledgeBase.domain.vo;

public class QaKnowledgeBaseDetailVo
{
    private Long id;

    private String kbName;

    private String kbDesc;

    private Boolean difyEnabled;

    private String difyDatasetId;

    private Boolean hasDifyDatasetApiKey;

    private String difyIndexingTechnique;

    private String difyDocForm;

    private String difyDocLanguage;

    private String difyProcessRuleMode;

    private String difyRuleSeparator;

    private Integer difyRuleMaxTokens;

    private Integer difyRuleChunkOverlap;

    private Boolean difyRemoveExtraSpaces;

    private Boolean difyRemoveUrlsEmails;

    private Boolean apiEnabled;

    private String apiBaseUrl;

    private Boolean hasApiAuthToken;

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

    public Boolean getHasDifyDatasetApiKey()
    {
        return hasDifyDatasetApiKey;
    }

    public void setHasDifyDatasetApiKey(Boolean hasDifyDatasetApiKey)
    {
        this.hasDifyDatasetApiKey = hasDifyDatasetApiKey;
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

    public Boolean getHasApiAuthToken()
    {
        return hasApiAuthToken;
    }

    public void setHasApiAuthToken(Boolean hasApiAuthToken)
    {
        this.hasApiAuthToken = hasApiAuthToken;
    }

    public Long getFileCount()
    {
        return fileCount;
    }

    public void setFileCount(Long fileCount)
    {
        this.fileCount = fileCount;
    }
}
