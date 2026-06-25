package com.ruoyi.qa.QAFile.controller;

import java.io.InputStream;
import java.io.Closeable;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletResponse;

import com.ruoyi.qa.QAFile.domain.QaFile;
import com.ruoyi.qa.QAFile.domain.enums.QaFileStatus;
import com.ruoyi.qa.QAFile.domain.vo.QaFileListVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.apache.commons.io.FilenameUtils;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.qa.QAFile.service.IQaFileService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * QA文件管理Controller
 *
 * @author ruoyi
 * @date 2026-06-13
 */
@RestController
@RequestMapping("/qa/QAfile")
public class QaFileController extends BaseController
{
    @Autowired
    private IQaFileService qaFileService;

    /**
     * 查询QA文件管理列表
     */
//    @PreAuthorize("@ss.hasPermi('qa:QAfile:list')")
    @GetMapping("/list")
    public TableDataInfo list(QaFile qaFile)
    {
        startPage();
        List<QaFile> list = qaFileService.selectQaFileList(qaFile);
        List<QaFileListVo> voList = list.stream().map(this::toListVo).collect(Collectors.toList());
        TableDataInfo dataTable = getDataTable(list);
        dataTable.setRows(voList);
        return dataTable;
    }

    private QaFileListVo toListVo(QaFile qaFile)
    {
        QaFileListVo vo = new QaFileListVo();
        vo.setId(qaFile.getId());
        vo.setFileName(qaFile.getFileName());
        vo.setFileSize(qaFile.getFileSize());
        vo.setFileType(qaFile.getFileType());
        vo.setStatus(qaFile.getStatus());
        vo.setIsDeleted(qaFile.getIsDeleted());
        return vo;
    }

    /**
     * 导出QA文件管理列表
     */
//    @PreAuthorize("@ss.hasPermi('qa:QAfile:export')")
    @Log(title = "QA文件管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, QaFile qaFile)
    {
        List<QaFile> list = qaFileService.selectQaFileList(qaFile);
        ExcelUtil<QaFile> util = new ExcelUtil<QaFile>(QaFile.class);
        util.exportExcel(response, list, "QA文件管理数据");
    }

    /**
     * 获取QA文件管理详细信息
     */
//    @PreAuthorize("@ss.hasPermi('qa:QAfile:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(qaFileService.selectQaFileById(id));
    }

    /**
     * 新增QA文件管理
     */
//    @PreAuthorize("@ss.hasPermi('qa:QAfile:add')")
    @Log(title = "QA文件管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody QaFile qaFile)
    {
        return toAjax(qaFileService.insertQaFile(qaFile));
    }

    /**
     * 修改QA文件管理
     */
//    @PreAuthorize("@ss.hasPermi('qa:QAfile:edit')")
    @Log(title = "QA文件管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody QaFile qaFile)
    {
        return toAjax(qaFileService.updateQaFile(qaFile));
    }

    @PostMapping("/upload")
    public AjaxResult upload(@RequestParam("file") MultipartFile file, @RequestParam(value = "id", required = false) Long id)
    {
        if (file == null || file.isEmpty())
        {
            return AjaxResult.error("文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        String ext = FilenameUtils.getExtension(originalFilename == null ? "" : originalFilename).toLowerCase();

        QaFile qaFile;
        if (id != null)
        {
            qaFile = qaFileService.selectQaFileById(id);
            if (qaFile == null)
            {
                return AjaxResult.error("记录不存在");
            }
        }
        else
        {
            qaFile = new QaFile();
            if (qaFile.getIsDeleted() == null)
            {
                qaFile.setIsDeleted(false);
            }
        }

        qaFile.setFileName(originalFilename);
        qaFile.setFileSize(file.getSize());
        qaFile.setFileType(ext);
        qaFile.setStatus((short) QaFileStatus.NORMAL.getCode());

        try
        {
            qaFile.setFileContent(extractText(file, ext));
        }
        catch (Exception e)
        {
            if (id != null)
            {
                QaFile patch = new QaFile();
                patch.setId(id);
                patch.setStatus((short) QaFileStatus.UPLOAD_FAILED.getCode());
                qaFileService.updateQaFile(patch);
            }
            return AjaxResult.error("解析文件失败：" + e.getMessage());
        }

        int rows = (id == null) ? qaFileService.insertQaFile(qaFile) : qaFileService.updateQaFile(qaFile);
        if (rows > 0)
        {
            return AjaxResult.success(qaFileService.selectQaFileById(qaFile.getId()));
        }
        if (id != null)
        {
            QaFile patch = new QaFile();
            patch.setId(id);
            patch.setStatus((short) QaFileStatus.UPLOAD_FAILED.getCode());
            qaFileService.updateQaFile(patch);
        }
        return AjaxResult.error("保存失败");
    }

    private String extractText(MultipartFile file, String ext) throws Exception
    {
        if ("txt".equals(ext))
        {
            return new String(file.getBytes(), StandardCharsets.UTF_8);
        }
        if ("pdf".equals(ext))
        {
            try (InputStream is = file.getInputStream(); PDDocument document = PDDocument.load(is))
            {
                PDFTextStripper stripper = new PDFTextStripper();
                return stripper.getText(document);
            }
        }
        if ("docx".equals(ext))
        {
            try (InputStream is = file.getInputStream(); XWPFDocument document = new XWPFDocument(is); XWPFWordExtractor extractor = new XWPFWordExtractor(document))
            {
                return extractor.getText();
            }
        }
        if ("doc".equals(ext))
        {
            try (InputStream is = file.getInputStream())
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

    /**
     * 删除QA文件管理
     */
//    @PreAuthorize("@ss.hasPermi('qa:QAfile:remove')")
    @Log(title = "QA文件管理", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(qaFileService.deleteQaFileByIds(ids));
    }
}
