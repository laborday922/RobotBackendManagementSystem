package com.ruoyi.qa.QAFile.mapper;

import java.util.List;

import com.ruoyi.qa.QAFile.domain.QaFile;

/**
 * QA文件管理Mapper接口
 *
 * @author ruoyi
 * @date 2026-06-13
 */
public interface QaFileMapper
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
     * 删除QA文件管理
     *
     * @param id QA文件管理主键
     * @return 结果
     */
    public int deleteQaFileById(Long id);

    /**
     * 批量删除QA文件管理
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteQaFileByIds(Long[] ids);
}
