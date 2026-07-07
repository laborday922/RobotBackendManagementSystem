package com.ruoyi.qa.Dify.dto;

import com.alibaba.fastjson2.annotation.JSONField;

public class DifyDocumentUpsertResponse
{
    @JSONField(name = "document")
    private DifyDocument document;

    @JSONField(name = "batch")
    private String batch;

    public DifyDocument getDocument()
    {
        return document;
    }

    public void setDocument(DifyDocument document)
    {
        this.document = document;
    }

    public String getBatch()
    {
        return batch;
    }

    public void setBatch(String batch)
    {
        this.batch = batch;
    }
}

