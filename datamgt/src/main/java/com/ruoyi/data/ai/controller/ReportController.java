package com.ruoyi.data.ai.controller;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.data.ai.controller.dto.ReportGenerateRequestDto;
import com.ruoyi.data.ai.controller.dto.ReportQueryDto;
import com.ruoyi.data.ai.service.ReportService;
import com.ruoyi.data.ai.service.vo.ReportDetailVo;
import com.ruoyi.data.ai.service.vo.ReportVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/ai/reports")
public class ReportController extends BaseController {

    @Autowired
    private ReportService reportService;

    /**
     * 获取ai报告
     * @RequestBody ReportGenerateRequest  用户问题
     * @return AI回复
     */
    @PostMapping("/generate")
    public AjaxResult generate(@RequestBody ReportGenerateRequestDto request) {
        if (request.getReportType() == null || request.getStartDate() == null || request.getEndDate() == null) {
            return error("参数不能为空");
        }
        String result = reportService.generateReport(
                request.getReportType(),
                request.getStartDate(),
                request.getEndDate(),
                request.getAnalysisDimension(),
                request.getCustomPrompt(),
                request.getReportDepth()
        );
        return success(result);
    }

    /**
     * 获取报告列表
     * @return list
     */
    @GetMapping
    public AjaxResult list(ReportQueryDto query) {
        //ruoyi自带分页
        startPage(query.getPage(), query.getSize());

        List<ReportVo> list = reportService.listReports(query);

        return AjaxResult.success(getDataTable(list));
    }

    /**
     * 下载报告文档
     */
    @GetMapping("/{id}/download")
    public void download(
            @PathVariable Long id,
            @RequestParam String format,
            HttpServletResponse response) {

        reportService.downloadReport(id, format, response);
    }

    /**
     * 查看报告详情
     */
    @GetMapping("/{id}")
    public AjaxResult getDetail(@PathVariable Long id) {
        ReportDetailVo report = reportService.getDetailById(id);
        if (report == null) {
            return error("报告不存在");
        }
        return success(report);
    }

    /**
     * 删除报告
     */
    @DeleteMapping("/{id}")
    public AjaxResult delete(@PathVariable Long id) {
        int rows = reportService.deleteById(id);
        if (rows > 0) {
            return success();
        }
        return error("删除失败");
    }
}