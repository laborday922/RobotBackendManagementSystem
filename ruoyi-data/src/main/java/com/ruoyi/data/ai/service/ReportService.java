package com.ruoyi.data.ai.service;

import com.ruoyi.data.ai.controller.dto.ReportQueryDto;
import com.ruoyi.data.ai.service.vo.ReportDetailVo;
import com.ruoyi.data.ai.service.vo.ReportVo;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

public interface ReportService {
    //获取ai分析报告
    String generateReport(String reportType,
                          String startDate,
                          String endDate,
                          String analysisDimension,
                          String customPrompt,
                          String reportDepth);

    //获取历史报告列表
    List<ReportVo> listReports(ReportQueryDto query);

    //报告文件下载
    void downloadReport(Long id, String format, HttpServletResponse response);

    //查看报告内容
    ReportDetailVo getDetailById(Long id);

    //删除报告记录
    int deleteById(Long id);
}
