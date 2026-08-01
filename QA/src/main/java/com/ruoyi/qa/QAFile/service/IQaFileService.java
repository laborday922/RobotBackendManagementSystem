package com.ruoyi.qa.QAFile.service;

import java.util.List;

import com.ruoyi.qa.QAFile.domain.QaFile;
import org.springframework.web.multipart.MultipartFile;

/**
 * QA文件管理Service接口
 *
 * @author ruoyi
 * @date 2026-06-13
 */
public interface IQaFileService
{
    /**
     * 查询QA文件管理
     *
     * @param id QA文件管理主键
     * @return QA文件管理
     */
    public QaFile selectQaFileById(Long id);

    /**
     * 查询QA文件管理列表
     *
     * @param qaFile QA文件管理
     * @return QA文件管理集合
     */
    public List<QaFile> selectQaFileList(QaFile qaFile);

    /**
     * 新增QA文件管理
     *
     * @param qaFile QA文件管理
     * @return 结果
     */
    public int insertQaFile(QaFile qaFile);

    /**
     * 修改QA文件管理
     *
     * @param qaFile QA文件管理
     * @return 结果
     */
    public int updateQaFile(QaFile qaFile);

    /**
     * 批量删除QA文件管理
     *
     * @param ids 需要删除的QA文件管理主键集合
     * @return 结果
     */
    public int deleteQaFileByIds(Long[] ids);

    /**
     * 删除QA文件管理信息
     *
     * @param id QA文件管理主键
     * @return 结果
     */
    public int deleteQaFileById(Long id);

    public boolean retryKgBuild(Long id);

    public int deleteQaFileByKnowledgeBaseIds(Long[] knowledgeBaseIds);

    public QaFile uploadAndProcess(MultipartFile file, Long id, Long knowledgeBaseId);

    public QaFile retryProcess(Long id);
}
