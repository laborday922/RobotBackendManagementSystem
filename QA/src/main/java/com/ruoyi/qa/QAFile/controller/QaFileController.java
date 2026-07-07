package com.ruoyi.qa.QAFile.controller;

import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletResponse;

import com.ruoyi.qa.QAFile.domain.QaFile;
import com.ruoyi.qa.QAFile.domain.vo.QaFileListVo;
import com.ruoyi.qa.Dify.DifyDatasetClient;
import com.ruoyi.qa.Dify.dto.DifyListDocumentsResponse;
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

    @Autowired
    private DifyDatasetClient difyDatasetClient;

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
        QaFile qaFile = qaFileService.uploadAndProcess(file, id);
        if (qaFile == null)
        {
            return AjaxResult.error("上传失败");
        }
        return AjaxResult.success(qaFile);
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

    @PostMapping("/{id}/kg/retry")
    public AjaxResult retryKg(@PathVariable("id") Long id)
    {
        QaFile qaFile = qaFileService.retryProcess(id);
        if (qaFile == null)
        {
            return AjaxResult.error("重试失败");
        }
        return AjaxResult.success(qaFile);
    }

    @PostMapping("/{id}/retry")
    public AjaxResult retry(@PathVariable("id") Long id)
    {
        QaFile qaFile = qaFileService.retryProcess(id);
        if (qaFile == null)
        {
            return AjaxResult.error("重试失败");
        }
        return AjaxResult.success(qaFile);
    }

    @GetMapping("/dify/documents")
    public AjaxResult listDifyDocuments(@RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
        @RequestParam(value = "limit", required = false, defaultValue = "20") Integer limit) throws Exception
    {
        DifyListDocumentsResponse resp = difyDatasetClient.listDocuments(page == null ? 1 : page, limit == null ? 20 : limit);
        return AjaxResult.success(resp);
    }
}
