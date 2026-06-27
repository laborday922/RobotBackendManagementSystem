package com.ruoyi.qa.QAFile.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ruoyi.qa.QAFile.domain.QaFile;
import com.ruoyi.qa.QAFile.domain.enums.QaFileStatus;
import com.ruoyi.qa.QAFile.mapper.QaFileMapper;
import com.ruoyi.qa.KG.KgOkResponse;
import com.ruoyi.qa.KG.KgPythonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.qa.QAFile.service.IQaFileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * QA文件管理Service业务层处理
 *
 * @author ruoyi
 * @date 2026-06-13
 */
@Service
public class QaFileServiceImpl implements IQaFileService
{
    private static final Logger log = LoggerFactory.getLogger(QaFileServiceImpl.class);

    @Autowired
    private QaFileMapper qaFileMapper;

    @Autowired
    private KgPythonClient kgPythonClient;

    /**
     * 查询QA文件管理
     *
     * @param id QA文件管理主键
     * @return QA文件管理
     */
    @Override
    public QaFile selectQaFileById(Long id)
    {
        return qaFileMapper.selectQaFileById(id);
    }

    /**
     * 查询QA文件管理列表
     *
     * @param qaFile QA文件管理
     * @return QA文件管理
     */
    @Override
    public List<QaFile> selectQaFileList(QaFile qaFile)
    {
        return qaFileMapper.selectQaFileList(qaFile);
    }

    /**
     * 新增QA文件管理
     *
     * @param qaFile QA文件管理
     * @return 结果
     */
    @Override
    public int insertQaFile(QaFile qaFile)
    {
        int rows = qaFileMapper.insertQaFile(qaFile);
        if (rows > 0)
        {
            syncUpsertToKg(qaFile);
        }
        return rows;
    }

    /**
     * 修改QA文件管理
     *
     * @param qaFile QA文件管理
     * @return 结果
     */
    @Override
    public int updateQaFile(QaFile qaFile)
    {
        int rows = qaFileMapper.updateQaFile(qaFile);
        if (rows > 0 && qaFile != null && qaFile.getFileContent() != null)
        {
            QaFile full = qaFile;
            if (qaFile.getFileContent() == null || qaFile.getFileName() == null)
            {
                full = qaFileMapper.selectQaFileById(qaFile.getId());
            }
            syncUpsertToKg(full);
        }
        return rows;
    }

    /**
     * 批量删除QA文件管理
     *
     * @param ids 需要删除的QA文件管理主键
     * @return 结果
     */
    @Override
    public int deleteQaFileByIds(Long[] ids)
    {
        if (ids != null)
        {
            for (Long id : ids)
            {
                if (id != null)
                {
                    syncDeleteToKg(id);
                }
            }
        }
        return qaFileMapper.deleteQaFileByIds(ids);
    }

    /**
     * 删除QA文件管理信息
     *
     * @param id QA文件管理主键
     * @return 结果
     */
    @Override
    public int deleteQaFileById(Long id)
    {
        if (id != null)
        {
            syncDeleteToKg(id);
        }
        return qaFileMapper.deleteQaFileById(id);
    }

    @Override
    public boolean retryKgBuild(Long id)
    {
        if (id == null)
        {
            return false;
        }
        QaFile qaFile = qaFileMapper.selectQaFileById(id);
        return syncUpsertToKg(qaFile);
    }

    private boolean syncUpsertToKg(QaFile qaFile)
    {
        if (qaFile == null || qaFile.getId() == null)
        {
            return false;
        }
        try
        {
            Map<String, Object> metadata = new HashMap<>();
            if (qaFile.getFileType() != null)
            {
                metadata.put("file_type", qaFile.getFileType());
            }
            if (qaFile.getFileSize() != null)
            {
                metadata.put("file_size", qaFile.getFileSize());
            }
            KgOkResponse resp = kgPythonClient.upsert(
                kgPythonClient.toUpsertRequest(
                    String.valueOf(qaFile.getId()),
                    qaFile.getFileName(),
                    qaFile.getFileContent(),
                    metadata
                )
            );

            if (resp != null && Boolean.TRUE.equals(resp.getOk()))
            {
                updateStatus(qaFile.getId(), (short) QaFileStatus.NORMAL.getCode());
                return true;
            }
            else
            {
                updateStatus(qaFile.getId(), (short) QaFileStatus.KG_BUILD_FAILED.getCode());
                return false;
            }
        }
        catch (Exception e)
        {
            log.warn("KG upsert exception (id={}): {}", qaFile.getId(), e.getMessage());
            updateStatus(qaFile.getId(), (short) QaFileStatus.KG_BUILD_FAILED.getCode());
            return false;
        }
    }

    private void syncDeleteToKg(Long id)
    {
        try
        {
            KgOkResponse resp = kgPythonClient.delete(String.valueOf(id));
            if (resp == null || !Boolean.TRUE.equals(resp.getOk()))
            {
                log.warn("KG delete returned not ok (id={})", id);
            }
        }
        catch (Exception e)
        {
            log.warn("KG delete exception (id={}): {}", id, e.getMessage());
        }
    }

    private void updateStatus(Long id, Short status)
    {
        QaFile patch = new QaFile();
        patch.setId(id);
        patch.setStatus(status);
        qaFileMapper.updateQaFile(patch);
    }
}
