package com.ruoyi.qa.QAFile.service.impl;

import java.io.Closeable;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.file.FileUploadUtils;
import com.ruoyi.common.utils.file.FileUtils;
import com.ruoyi.qa.Dify.DifyDatasetClient;
import com.ruoyi.qa.Dify.dto.DifyDocumentByTextRequest;
import com.ruoyi.qa.Dify.dto.DifyDocumentUpsertResponse;
import com.ruoyi.qa.KnowledgeBase.domain.QaKnowledgeBase;
import com.ruoyi.qa.KnowledgeBase.mapper.QaKnowledgeBaseMapper;
import com.ruoyi.qa.QAFile.domain.QaFile;
import com.ruoyi.qa.QAFile.domain.enums.QaFileStatus;
import com.ruoyi.qa.QAFile.mapper.QaFileMapper;
import com.ruoyi.qa.KG.KgOkResponse;
import com.ruoyi.qa.KG.KgPythonClient;
import org.apache.commons.io.FilenameUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.qa.QAFile.service.IQaFileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

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
    private QaKnowledgeBaseMapper qaKnowledgeBaseMapper;

    @Autowired
    private KgPythonClient kgPythonClient;

    @Autowired
    private DifyDatasetClient difyDatasetClient;

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
        return qaFileMapper.insertQaFile(qaFile);
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
        return qaFileMapper.updateQaFile(qaFile);
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
                    QaFile qaFile = qaFileMapper.selectQaFileById(id);
                    syncDeleteToKg(qaFile);
                    syncDeleteToDify(qaFile);
                    deleteLocalFile(qaFile);
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
            QaFile qaFile = qaFileMapper.selectQaFileById(id);
            syncDeleteToKg(qaFile);
            syncDeleteToDify(qaFile);
            deleteLocalFile(qaFile);
        }
        return qaFileMapper.deleteQaFileById(id);
    }

    @Override
    public int deleteQaFileByKnowledgeBaseIds(Long[] knowledgeBaseIds)
    {
        if (knowledgeBaseIds == null || knowledgeBaseIds.length == 0)
        {
            return 0;
        }
        List<QaFile> files = qaFileMapper.selectQaFileByKnowledgeBaseIds(knowledgeBaseIds);
        int count = 0;
        if (files == null)
        {
            return 0;
        }
        for (QaFile qaFile : files)
        {
            if (qaFile != null && qaFile.getId() != null)
            {
                count += deleteQaFileById(qaFile.getId());
            }
        }
        return count;
    }

    @Override
    public boolean retryKgBuild(Long id)
    {
        QaFile qaFile = retryProcess(id);
        return qaFile != null && qaFile.getStatus() != null && qaFile.getStatus() == QaFileStatus.NORMAL.getCode();
    }

    @Override
    public QaFile uploadAndProcess(MultipartFile file, Long id, Long knowledgeBaseId)
    {
        if (file == null || file.isEmpty())
        {
            return null;
        }

        String originalFilename = file.getOriginalFilename();
        String ext = FilenameUtils.getExtension(originalFilename == null ? "" : originalFilename).toLowerCase();
        QaFile qaFile = (id == null) ? new QaFile() : qaFileMapper.selectQaFileById(id);
        if (id != null && qaFile == null)
        {
            return null;
        }

        if (id == null)
        {
            if (knowledgeBaseId == null)
            {
                throw new ServiceException("请选择所属知识库");
            }
            QaKnowledgeBase knowledgeBase = qaKnowledgeBaseMapper.selectQaKnowledgeBaseById(knowledgeBaseId);
            if (knowledgeBase == null)
            {
                throw new ServiceException("所属知识库不存在");
            }
            qaFile.setKnowledgeBaseId(knowledgeBaseId);
        }
        else if (knowledgeBaseId != null)
        {
            qaFile.setKnowledgeBaseId(knowledgeBaseId);
        }

        String oldPath = qaFile.getPath();
        String savedPath;
        try
        {
            savedPath = FileUploadUtils.upload(RuoYiConfig.getUploadPath(), file);
        }
        catch (Exception e)
        {
            qaFile.setFileName(originalFilename);
            qaFile.setFileSize(file.getSize());
            qaFile.setFileType(ext);
            qaFile.setStatus((short) QaFileStatus.UPLOAD_FAILED.getCode());
            if (id == null)
            {
                qaFileMapper.insertQaFile(qaFile);
            }
            else
            {
                qaFileMapper.updateQaFile(qaFile);
            }
            return qaFileMapper.selectQaFileById(qaFile.getId());
        }

        if (StringUtils.hasText(oldPath))
        {
            deleteLocalFile(oldPath);
        }

        qaFile.setFileName(originalFilename);
        qaFile.setFileSize(file.getSize());
        qaFile.setFileType(ext);
        qaFile.setPath(savedPath);

        try
        {
            qaFile.setFileContent(extractTextFromLocal(savedPath, ext));
        }
        catch (Exception e)
        {
            qaFile.setFileContent("");
            qaFile.setStatus((short) QaFileStatus.UPLOAD_FAILED.getCode());
            if (id == null)
            {
                qaFileMapper.insertQaFile(qaFile);
            }
            else
            {
                qaFileMapper.updateQaFile(qaFile);
            }
            return qaFileMapper.selectQaFileById(qaFile.getId());
        }

        if (id == null)
        {
            qaFileMapper.insertQaFile(qaFile);
        }
        else
        {
            qaFileMapper.updateQaFile(qaFile);
        }

        QaFile persisted = qaFileMapper.selectQaFileById(qaFile.getId());
        processDifyThenKg(persisted);
        return qaFileMapper.selectQaFileById(qaFile.getId());
    }

    @Override
    public QaFile retryProcess(Long id)
    {
        if (id == null)
        {
            return null;
        }
        QaFile qaFile = qaFileMapper.selectQaFileById(id);
        if (qaFile == null)
        {
            return null;
        }

        if (qaFile.getStatus() != null && qaFile.getStatus() == QaFileStatus.KG_BUILD_FAILED.getCode()
            && StringUtils.hasText(qaFile.getDifyDocumentId())
            && shouldSyncApi(resolveKnowledgeBase(qaFile)))
        {
            syncUpsertToKg(qaFile);
            return qaFileMapper.selectQaFileById(id);
        }

        if (!StringUtils.hasText(qaFile.getFileContent()))
        {
            try
            {
                String ext = qaFile.getFileType();
                if (!StringUtils.hasText(ext) && StringUtils.hasText(qaFile.getFileName()))
                {
                    ext = FilenameUtils.getExtension(qaFile.getFileName()).toLowerCase();
                }
                qaFile.setFileContent(extractTextFromLocal(qaFile.getPath(), ext));
                qaFileMapper.updateQaFile(qaFile);
                qaFile = qaFileMapper.selectQaFileById(id);
            }
            catch (Exception e)
            {
                updateStatus(id, (short) QaFileStatus.UPLOAD_FAILED.getCode());
                return qaFileMapper.selectQaFileById(id);
            }
        }

        processDifyThenKg(qaFile);
        return qaFileMapper.selectQaFileById(id);
    }

    private void processDifyThenKg(QaFile qaFile)
    {
        if (qaFile == null || qaFile.getId() == null)
        {
            return;
        }
        if (!StringUtils.hasText(qaFile.getFileContent()))
        {
            updateStatus(qaFile.getId(), (short) QaFileStatus.UPLOAD_FAILED.getCode());
            return;
        }

        QaKnowledgeBase knowledgeBase = resolveKnowledgeBase(qaFile);
        if (shouldSyncDify(knowledgeBase))
        {
            try
            {
                DifyDocumentUpsertResponse resp;
                DifyDocumentByTextRequest difyRequest = buildDifyRequest(knowledgeBase, qaFile);
                if (StringUtils.hasText(qaFile.getDifyDocumentId()))
                {
                    resp = difyDatasetClient.updateDocumentByText(
                        knowledgeBase == null ? null : knowledgeBase.getDifyDatasetId(),
                        knowledgeBase == null ? null : knowledgeBase.getDifyDatasetApiKey(),
                        qaFile.getDifyDocumentId(),
                        difyRequest
                    );
                }
                else
                {
                    resp = difyDatasetClient.createDocumentByText(
                        knowledgeBase == null ? null : knowledgeBase.getDifyDatasetId(),
                        knowledgeBase == null ? null : knowledgeBase.getDifyDatasetApiKey(),
                        difyRequest
                    );
                }

                String docId = resp == null || resp.getDocument() == null ? null : resp.getDocument().getId();
                if (!StringUtils.hasText(docId))
                {
                    log.warn("Dify upsert returned empty document id (qaFileId={}): {}", qaFile.getId(), resp == null ? null : resp.getBatch());
                    updateStatus(qaFile.getId(), (short) QaFileStatus.DIFY_UPLOAD_FAILED.getCode());
                    return;
                }
                if (StringUtils.hasText(docId) && !docId.equals(qaFile.getDifyDocumentId()))
                {
                    QaFile patch = new QaFile();
                    patch.setId(qaFile.getId());
                    patch.setDifyDocumentId(docId);
                    qaFileMapper.updateQaFile(patch);
                    qaFile.setDifyDocumentId(docId);
                }
            }
            catch (Exception e)
            {
                log.warn("Dify upsert exception (qaFileId={}): {}", qaFile.getId(), e.getMessage());
                updateStatus(qaFile.getId(), (short) QaFileStatus.DIFY_UPLOAD_FAILED.getCode());
                return;
            }
        }

        syncUpsertToKg(qaFile);
    }

    private boolean syncUpsertToKg(QaFile qaFile)
    {
        if (qaFile == null || qaFile.getId() == null)
        {
            return false;
        }
        QaKnowledgeBase knowledgeBase = resolveKnowledgeBase(qaFile);
        if (!shouldSyncApi(knowledgeBase))
        {
            updateStatus(qaFile.getId(), (short) QaFileStatus.NORMAL.getCode());
            return true;
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
            if (qaFile.getKnowledgeBaseId() != null)
            {
                metadata.put("knowledge_base_id", qaFile.getKnowledgeBaseId());
            }
            if (StringUtils.hasText(qaFile.getKnowledgeBaseName()))
            {
                metadata.put("knowledge_base_name", qaFile.getKnowledgeBaseName());
            }
            KgOkResponse resp = kgPythonClient.upsert(
                knowledgeBase == null ? null : knowledgeBase.getApiBaseUrl(),
                knowledgeBase == null ? null : knowledgeBase.getApiAuthToken(),
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

    private void syncDeleteToDify(QaFile qaFile)
    {
        if (qaFile == null || !StringUtils.hasText(qaFile.getDifyDocumentId()))
        {
            return;
        }
        QaKnowledgeBase knowledgeBase = resolveKnowledgeBase(qaFile);
        if (!shouldSyncDify(knowledgeBase))
        {
            return;
        }
        try
        {
            difyDatasetClient.deleteDocument(
                knowledgeBase == null ? null : knowledgeBase.getDifyDatasetId(),
                knowledgeBase == null ? null : knowledgeBase.getDifyDatasetApiKey(),
                qaFile.getDifyDocumentId()
            );
        }
        catch (Exception e)
        {
            log.warn("Dify delete exception (id={}): {}", qaFile.getId(), e.getMessage());
        }
    }

    private void syncDeleteToKg(QaFile qaFile)
    {
        if (qaFile == null || qaFile.getId() == null)
        {
            return;
        }
        QaKnowledgeBase knowledgeBase = resolveKnowledgeBase(qaFile);
        if (!shouldSyncApi(knowledgeBase))
        {
            return;
        }
        try
        {
            KgOkResponse resp = kgPythonClient.delete(
                knowledgeBase == null ? null : knowledgeBase.getApiBaseUrl(),
                knowledgeBase == null ? null : knowledgeBase.getApiAuthToken(),
                String.valueOf(qaFile.getId())
            );
            if (resp == null || !Boolean.TRUE.equals(resp.getOk()))
            {
                log.warn("KG delete returned not ok (id={})", qaFile.getId());
            }
        }
        catch (Exception e)
        {
            log.warn("KG delete exception (id={}): {}", qaFile.getId(), e.getMessage());
        }
    }

    private void updateStatus(Long id, Short status)
    {
        QaFile patch = new QaFile();
        patch.setId(id);
        patch.setStatus(status);
        qaFileMapper.updateQaFile(patch);
    }

    private String extractTextFromLocal(String resourcePath, String ext) throws Exception
    {
        if (!StringUtils.hasText(resourcePath))
        {
            throw new IllegalArgumentException("path is blank");
        }
        if (!StringUtils.hasText(ext))
        {
            ext = "";
        }
        Path localPath = resolveResourceToLocalPath(resourcePath);
        if ("txt".equals(ext))
        {
            return Files.readString(localPath, StandardCharsets.UTF_8);
        }
        if ("pdf".equals(ext))
        {
            try (InputStream is = new FileInputStream(localPath.toFile()); PDDocument document = PDDocument.load(is))
            {
                PDFTextStripper stripper = new PDFTextStripper();
                return stripper.getText(document);
            }
        }
        if ("docx".equals(ext))
        {
            try (InputStream is = new FileInputStream(localPath.toFile()); XWPFDocument document = new XWPFDocument(is); XWPFWordExtractor extractor = new XWPFWordExtractor(document))
            {
                return extractor.getText();
            }
        }
        if ("doc".equals(ext))
        {
            try (InputStream is = new FileInputStream(localPath.toFile()))
            {
                Object document = null;
                Object extractor = null;
                try
                {
                    Class<?> hwpfDocumentClass = Class.forName("org.apache.poi.hwpf.HWPFDocument");
                    document = hwpfDocumentClass.getConstructor(InputStream.class).newInstance(is);
                    Class<?> wordExtractorClass = Class.forName("org.apache.poi.hwpf.extractor.WordExtractor");
                    extractor = wordExtractorClass.getConstructor(hwpfDocumentClass).newInstance(document);
                    return (String) wordExtractorClass.getMethod("getText").invoke(extractor);
                }
                catch (ClassNotFoundException e)
                {
                    throw new IllegalStateException("缺少解析doc所需依赖：org.apache.poi:poi-scratchpad", e);
                }
                finally
                {
                    if (extractor instanceof Closeable)
                    {
                        ((Closeable) extractor).close();
                    }
                    if (document instanceof Closeable)
                    {
                        ((Closeable) document).close();
                    }
                }
            }
        }
        return "";
    }

    private static Path resolveResourceToLocalPath(String resourcePath)
    {
        if (resourcePath.contains(":\\") || resourcePath.startsWith("/") == false && resourcePath.startsWith("\\") == false)
        {
            Path p = Paths.get(resourcePath);
            if (p.isAbsolute())
            {
                return p;
            }
        }
        String localPath = RuoYiConfig.getProfile() + FileUtils.stripPrefix(resourcePath);
        return Paths.get(localPath);
    }

    private QaKnowledgeBase resolveKnowledgeBase(QaFile qaFile)
    {
        if (qaFile == null || qaFile.getKnowledgeBaseId() == null)
        {
            return null;
        }
        return qaKnowledgeBaseMapper.selectQaKnowledgeBaseById(qaFile.getKnowledgeBaseId());
    }

    private boolean shouldSyncDify(QaKnowledgeBase knowledgeBase)
    {
        return knowledgeBase == null || Boolean.TRUE.equals(knowledgeBase.getDifyEnabled());
    }

    private boolean shouldSyncApi(QaKnowledgeBase knowledgeBase)
    {
        return knowledgeBase == null || Boolean.TRUE.equals(knowledgeBase.getApiEnabled());
    }

    private DifyDocumentByTextRequest buildDifyRequest(QaKnowledgeBase knowledgeBase, QaFile qaFile)
    {
        DifyDocumentByTextRequest req = new DifyDocumentByTextRequest();
        req.setName(qaFile.getFileName());
        req.setText(qaFile.getFileContent());

        if (knowledgeBase == null)
        {
            req.setIndexingTechnique("high_quality");
            req.setDocForm("qa_model");
            req.setDocLanguage("Chinese Simplified");
            return req;
        }

        req.setIndexingTechnique(StringUtils.hasText(knowledgeBase.getDifyIndexingTechnique()) ? knowledgeBase.getDifyIndexingTechnique() : "high_quality");
        req.setDocForm(StringUtils.hasText(knowledgeBase.getDifyDocForm()) ? knowledgeBase.getDifyDocForm() : "qa_model");
        req.setDocLanguage(StringUtils.hasText(knowledgeBase.getDifyDocLanguage()) ? knowledgeBase.getDifyDocLanguage() : "Chinese Simplified");

        String processMode = StringUtils.hasText(knowledgeBase.getDifyProcessRuleMode()) ? knowledgeBase.getDifyProcessRuleMode() : "custom";
        DifyDocumentByTextRequest.ProcessRule processRule = new DifyDocumentByTextRequest.ProcessRule();
        processRule.setMode(processMode);

        if ("custom".equals(processMode))
        {
            DifyDocumentByTextRequest.Rules rules = new DifyDocumentByTextRequest.Rules();
            rules.setPreProcessingRules(buildPreProcessingRules(knowledgeBase));

            DifyDocumentByTextRequest.Segmentation segmentation = new DifyDocumentByTextRequest.Segmentation();
            segmentation.setSeparator(knowledgeBase.getDifyRuleSeparator() == null ? "\n\n" : knowledgeBase.getDifyRuleSeparator());
            segmentation.setMaxTokens(knowledgeBase.getDifyRuleMaxTokens() == null ? 500 : knowledgeBase.getDifyRuleMaxTokens());
            segmentation.setChunkOverlap(knowledgeBase.getDifyRuleChunkOverlap() == null ? 50 : knowledgeBase.getDifyRuleChunkOverlap());
            rules.setSegmentation(segmentation);
            processRule.setRules(rules);
        }

        req.setProcessRule(processRule);
        return req;
    }

    private DifyDocumentByTextRequest.PreProcessingRuleItem[] buildPreProcessingRules(QaKnowledgeBase knowledgeBase)
    {
        DifyDocumentByTextRequest.PreProcessingRuleItem removeExtraSpaces = new DifyDocumentByTextRequest.PreProcessingRuleItem();
        removeExtraSpaces.setId("remove_extra_spaces");
        removeExtraSpaces.setEnabled(Boolean.TRUE.equals(knowledgeBase.getDifyRemoveExtraSpaces()));

        DifyDocumentByTextRequest.PreProcessingRuleItem removeUrlsEmails = new DifyDocumentByTextRequest.PreProcessingRuleItem();
        removeUrlsEmails.setId("remove_urls_emails");
        removeUrlsEmails.setEnabled(Boolean.TRUE.equals(knowledgeBase.getDifyRemoveUrlsEmails()));
        return new DifyDocumentByTextRequest.PreProcessingRuleItem[] { removeExtraSpaces, removeUrlsEmails };
    }

    private void deleteLocalFile(QaFile qaFile)
    {
        if (qaFile == null)
        {
            return;
        }
        deleteLocalFile(qaFile.getPath());
    }

    private void deleteLocalFile(String resourcePath)
    {
        if (!StringUtils.hasText(resourcePath))
        {
            return;
        }
        try
        {
            String localPath = RuoYiConfig.getProfile() + FileUtils.stripPrefix(resourcePath);
            FileUtils.deleteFile(localPath);
        }
        catch (Exception e)
        {
            log.warn("delete local file exception: {}", e.getMessage());
        }
    }
}
