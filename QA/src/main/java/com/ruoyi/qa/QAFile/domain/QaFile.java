package com.ruoyi.qa.QAFile.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * QA文件管理对象 qa_file
 *
 * @author ruoyi
 * @date 2026-06-13
 */
public class QaFile extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** $column.columnComment */
    private Long id;

    /** 文件名 */
    @Excel(name = "文件名")
    private String fileName;

    /** 文件原始文本内容 */
    @Excel(name = "文件原始文本内容")
    private String fileContent;

    /** 文件大小(字节) */
    @Excel(name = "文件大小(字节)")
    private Long fileSize;

    /** 文件类型(doc/docx/pdf) */
    @Excel(name = "文件类型(doc/docx/pdf)")
    private String fileType;

    /** 原始文件本地路径 */
    @Excel(name = "原始文件本地路径")
    private String path;

    /** dify 知识库文档ID */
    @Excel(name = "dify 知识库文档ID")
    private String difyDocumentId;

    /** 所属知识库ID */
    @Excel(name = "所属知识库ID")
    private Long knowledgeBaseId;

    /** 所属知识库名称 */
    private String knowledgeBaseName;

    /** 处理状态 */
    @Excel(name = "处理状态")
    private Short status;

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getId()
    {
        return id;
    }

    public void setFileName(String fileName)
    {
        this.fileName = fileName;
    }

    public String getFileName()
    {
        return fileName;
    }

    public void setFileContent(String fileContent)
    {
        this.fileContent = fileContent;
    }

    public String getFileContent()
    {
        return fileContent;
    }

    public void setFileSize(Long fileSize)
    {
        this.fileSize = fileSize;
    }

    public Long getFileSize()
    {
        return fileSize;
    }

    public void setFileType(String fileType)
    {
        this.fileType = fileType;
    }

    public String getFileType()
    {
        return fileType;
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    public String getPath()
    {
        return path;
    }

    public void setDifyDocumentId(String difyDocumentId)
    {
        this.difyDocumentId = difyDocumentId;
    }

    public String getDifyDocumentId()
    {
        return difyDocumentId;
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

    public void setStatus(Short status)
    {
        this.status = status;
    }

    public Short getStatus()
    {
        return status;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("fileName", getFileName())
            .append("fileContent", getFileContent())
            .append("fileSize", getFileSize())
            .append("fileType", getFileType())
            .append("path", getPath())
            .append("difyDocumentId", getDifyDocumentId())
            .append("knowledgeBaseId", getKnowledgeBaseId())
            .append("knowledgeBaseName", getKnowledgeBaseName())
            .append("status", getStatus())
            .toString();
    }
}
