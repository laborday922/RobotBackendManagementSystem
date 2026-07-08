package com.ruoyi.qa.Dify.dto;

import com.alibaba.fastjson2.annotation.JSONField;

public class DifyDocumentByTextRequest
{
    @JSONField(name = "name")
    private String name;

    @JSONField(name = "text")
    private String text;

    @JSONField(name = "indexing_technique")
    private String indexingTechnique;

    @JSONField(name = "doc_form")
    private String docForm;

    @JSONField(name = "doc_language")
    private String docLanguage;

    @JSONField(name = "process_rule")
    private ProcessRule processRule;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getText()
    {
        return text;
    }

    public void setText(String text)
    {
        this.text = text;
    }

    public String getIndexingTechnique()
    {
        return indexingTechnique;
    }

    public void setIndexingTechnique(String indexingTechnique)
    {
        this.indexingTechnique = indexingTechnique;
    }

    public String getDocForm()
    {
        return docForm;
    }

    public void setDocForm(String docForm)
    {
        this.docForm = docForm;
    }

    public String getDocLanguage()
    {
        return docLanguage;
    }

    public void setDocLanguage(String docLanguage)
    {
        this.docLanguage = docLanguage;
    }

    public ProcessRule getProcessRule()
    {
        return processRule;
    }

    public void setProcessRule(ProcessRule processRule)
    {
        this.processRule = processRule;
    }

    public static class ProcessRule
    {
        @JSONField(name = "mode")
        private String mode;

        @JSONField(name = "rules")
        private Rules rules;

        public String getMode()
        {
            return mode;
        }

        public void setMode(String mode)
        {
            this.mode = mode;
        }

        public Rules getRules()
        {
            return rules;
        }

        public void setRules(Rules rules)
        {
            this.rules = rules;
        }
    }

    public static class Rules
    {
        @JSONField(name = "pre_processing_rules")
        private PreProcessingRuleItem[] preProcessingRules;

        @JSONField(name = "segmentation")
        private Segmentation segmentation;

        @JSONField(name = "parent_mode")
        private String parentMode;

        @JSONField(name = "subchunk_segmentation")
        private Segmentation subchunkSegmentation;

        public PreProcessingRuleItem[] getPreProcessingRules()
        {
            return preProcessingRules;
        }

        public void setPreProcessingRules(PreProcessingRuleItem[] preProcessingRules)
        {
            this.preProcessingRules = preProcessingRules;
        }

        public Segmentation getSegmentation()
        {
            return segmentation;
        }

        public void setSegmentation(Segmentation segmentation)
        {
            this.segmentation = segmentation;
        }

        public String getParentMode()
        {
            return parentMode;
        }

        public void setParentMode(String parentMode)
        {
            this.parentMode = parentMode;
        }

        public Segmentation getSubchunkSegmentation()
        {
            return subchunkSegmentation;
        }

        public void setSubchunkSegmentation(Segmentation subchunkSegmentation)
        {
            this.subchunkSegmentation = subchunkSegmentation;
        }
    }

    public static class PreProcessingRuleItem
    {
        @JSONField(name = "id")
        private String id;

        @JSONField(name = "enabled")
        private Boolean enabled;

        public String getId()
        {
            return id;
        }

        public void setId(String id)
        {
            this.id = id;
        }

        public Boolean getEnabled()
        {
            return enabled;
        }

        public void setEnabled(Boolean enabled)
        {
            this.enabled = enabled;
        }
    }

    public static class Segmentation
    {
        @JSONField(name = "separator")
        private String separator;

        @JSONField(name = "max_tokens")
        private Integer maxTokens;

        @JSONField(name = "chunk_overlap")
        private Integer chunkOverlap;

        public String getSeparator()
        {
            return separator;
        }

        public void setSeparator(String separator)
        {
            this.separator = separator;
        }

        public Integer getMaxTokens()
        {
            return maxTokens;
        }

        public void setMaxTokens(Integer maxTokens)
        {
            this.maxTokens = maxTokens;
        }

        public Integer getChunkOverlap()
        {
            return chunkOverlap;
        }

        public void setChunkOverlap(Integer chunkOverlap)
        {
            this.chunkOverlap = chunkOverlap;
        }
    }
}
