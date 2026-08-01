package com.ruoyi.qa.QAFile.domain.vo;

public class QaFileListVo
{
    private Long id;

    private String fileName;

    private Long fileSize;

    private String fileType;

    private Long knowledgeBaseId;

    private String knowledgeBaseName;

    private Short status;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getFileName()
    {
        return fileName;
    }

    public void setFileName(String fileName)
    {
        this.fileName = fileName;
    }

    public Long getFileSize()
    {
        return fileSize;
    }

    public void setFileSize(Long fileSize)
    {
        this.fileSize = fileSize;
    }

    public String getFileType()
    {
        return fileType;
    }

    public void setFileType(String fileType)
    {
        this.fileType = fileType;
    }

    public Long getKnowledgeBaseId()
    {
        return knowledgeBaseId;
    }

    public void setKnowledgeBaseId(Long knowledgeBaseId)
    {
        this.knowledgeBaseId = knowledgeBaseId;
    }

    public String getKnowledgeBaseName()
    {
        return knowledgeBaseName;
    }

    public void setKnowledgeBaseName(String knowledgeBaseName)
    {
        this.knowledgeBaseName = knowledgeBaseName;
    }

    public Short getStatus()
    {
        return status;
    }

    public void setStatus(Short status)
    {
        this.status = status;
    }

}
