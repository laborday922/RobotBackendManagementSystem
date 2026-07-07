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

        public String getMode()
        {
            return mode;
        }

        public void setMode(String mode)
        {
            this.mode = mode;
        }
    }
}

