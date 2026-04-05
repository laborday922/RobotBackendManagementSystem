package com.ruoyi.data.ai.service.impl;

import com.ruoyi.data.ai.controller.dto.ReportQueryDto;
import com.ruoyi.data.ai.mapper.ReportContentMapper;
import com.ruoyi.data.ai.mapper.ReportDataMapper;
import com.ruoyi.data.ai.mapper.ReportMapper;
import com.ruoyi.data.ai.mapper.po.ReportContentPo;
import com.ruoyi.data.ai.mapper.po.ReportPo;
import com.ruoyi.data.ai.service.ReportService;
import com.ruoyi.data.ai.service.TongYiService;
import com.ruoyi.data.ai.service.vo.ReportDetailVo;
import com.ruoyi.data.ai.service.vo.ReportVo;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xhtmlrenderer.pdf.ITextRenderer;

import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private TongYiService tongYiService;

    @Autowired
    private ReportMapper reportMapper;

    @Autowired
    private ReportContentMapper contentMapper;

    @Autowired
    private ReportDataMapper reportDataMapper;

    @Override
    public String generateReport(String reportType,
                                 String startDate,
                                 String endDate,
                                 String analysisDimension,
                                 String customPrompt,
                                 String reportDepth) {

        // 参数校验
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("开始日期和结束日期不能为空");
        }
        if (reportDepth == null) {
            reportDepth = "standard"; // 默认标准版
        }

        // 1️⃣ 获取真实数据（从数据库）
        String rawData = fetchRawData(analysisDimension, startDate, endDate);

        // 2️⃣ 构建 AI 生成的完整 prompt（融合自定义要求、深度类型）
        String aiPrompt = buildAIPrompt(reportType, startDate, endDate, analysisDimension,
                rawData, customPrompt, reportDepth);

        // 3️⃣ 调用通义服务生成报告（假设有支持完整 prompt 的方法）
        String aiResult = tongYiService.chat(aiPrompt);

        // 4️⃣ 存储报告元数据
        ReportPo reportPO = new ReportPo();
        // 使用时间戳作为版本号
        String reportName = String.format("%s报告_%s_%s_v%d",
                reportType, startDate, endDate, System.currentTimeMillis() / 1000);
        reportPO.setReportName(reportName);
        reportPO.setReportType(reportType);
        reportPO.setStartDate(java.sql.Date.valueOf(startDate));
        reportPO.setEndDate(java.sql.Date.valueOf(endDate));
        reportPO.setStatus("success");
        reportPO.setCreatedAt(new Date());
        reportMapper.insertReport(reportPO);

        // 5️⃣ 存储报告内容
        ReportContentPo contentPO = new ReportContentPo();
        contentPO.setReportId(reportPO.getId());
        contentPO.setContent(aiResult);
        contentMapper.insertContent(contentPO);

        return aiResult;
    }

    /**
     * 根据各参数构建完整的 AI 提示词
     */
    private String buildAIPrompt(String reportType, String startDate, String endDate,
                                 String analysisDimension, String rawData,
                                 String customPrompt, String reportDepth) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请生成一份").append(reportType).append("。\n");
        prompt.append("时间范围：").append(startDate).append(" 至 ").append(endDate).append("\n");
        prompt.append("分析维度：").append(analysisDimension).append("\n");
        prompt.append("原始数据如下：\n").append(rawData).append("\n");
        if (customPrompt != null && !customPrompt.trim().isEmpty()) {
            prompt.append("用户额外要求：").append(customPrompt).append("\n");
        }
        prompt.append("报告深度：").append(reportDepth.equals("summary") ? "仅需摘要部分" : "完整报告（包含摘要和详细分析）").append("\n");
        prompt.append("请用中文生成报告。");
        return prompt.toString();
    }

    //获取历史报告列表
    @Override
    public List<ReportVo> listReports(ReportQueryDto query) {

        // 若依自带分页
        //startPage(query.getPage(), query.getSize());

        List<ReportPo> list = reportMapper.selectReportList(query);

        // PO → VO
        return list.stream().map(po -> {
            ReportVo vo = new ReportVo();
            vo.setId(po.getId());
            vo.setReportName(po.getReportName());
            vo.setReportType(po.getReportType());
            vo.setStatus(po.getStatus());
            vo.setCreatedAt(po.getCreatedAt());
            return vo;
        }).toList();
    }

    //报告文件下载
    @Override
    public void downloadReport(Long id, String format, HttpServletResponse response) {

        ReportPo report = reportMapper.selectById(id);
        if (report == null) {
            throw new RuntimeException("报告不存在");
        }

        ReportContentPo contentPO = contentMapper.selectContentByReportId(id);
        String markdown = contentPO.getContent();

        try {
            if ("pdf".equals(format)) {

                // ✅ 1. Markdown → HTML
                String htmlBody = markdownToHtml(markdown);

                // ✅ 2. 包装完整HTML（加样式）
                String fullHtml = buildHtml(htmlBody);

                // ✅ 3. 设置响应头
                response.setContentType("application/pdf");
                response.setHeader("Content-Disposition", "attachment;filename=report.pdf");

                // ✅ 4. 生成PDF
                ITextRenderer renderer = new ITextRenderer();

                // ⚠️ 中文字体（关键！！！）
                renderer.getFontResolver().addFont(
                        "C:/Windows/Fonts/simsun.ttc",
                        com.lowagie.text.pdf.BaseFont.IDENTITY_H,
                        com.lowagie.text.pdf.BaseFont.NOT_EMBEDDED
                );

                renderer.setDocumentFromString(fullHtml);
                renderer.layout();
                renderer.createPDF(response.getOutputStream());

            } else if ("html".equals(format)) {

                String htmlBody = markdownToHtml(markdown);
                String fullHtml = buildHtml(htmlBody);

                response.setContentType("text/html;charset=UTF-8");
                response.getWriter().write(fullHtml);

            } else {
                throw new RuntimeException("暂不支持该格式");
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("下载失败");
        }
    }

    public String buildHtml(String body) {
        return "<html>" +
                "<head>" +
                "<meta charset='UTF-8'/>" +
                "<style>" +
                "body { font-family: SimSun; padding: 20px; }" +
                "h1 { font-size: 24px; }" +
                "h2 { font-size: 20px; }" +
                "h3 { font-size: 16px; }" +

                "table { border-collapse: collapse; width: 100%; margin-top: 10px; }" +
                "th, td { border: 1px solid #333; padding: 6px; text-align: center; }" +
                "th { background-color: #f5f5f5; }" +

                "p { line-height: 1.6; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                body +
                "</body></html>";
    }

    public String markdownToHtml(String markdown) {
        MutableDataSet options = new MutableDataSet();

        // 开启表格支持（非常关键！！！）
        options.set(Parser.EXTENSIONS, Arrays.asList(
                TablesExtension.create()
        ));

        Parser parser = Parser.builder(options).build();
        HtmlRenderer renderer = HtmlRenderer.builder(options).build();

        return renderer.render(parser.parse(markdown));
    }

    /**
     * 查询报告详情（PO → VO）
     */
    @Override
    public ReportDetailVo getDetailById(Long id) {
        ReportPo report = reportMapper.selectById(id);
        if (report == null) {
            return null;
        }

        ReportContentPo contentPo = contentMapper.selectContentByReportId(id);

        ReportDetailVo vo = new ReportDetailVo();

        // 主表
        vo.setId(report.getId());
        vo.setReportName(report.getReportName());
        vo.setReportType(report.getReportType());
        vo.setStartDate(String.valueOf(report.getStartDate()));
        vo.setEndDate(String.valueOf(report.getEndDate()));
        vo.setStatus(report.getStatus());
        vo.setCreateTime(String.valueOf(report.getCreatedAt()));

        // 内容表
        if (contentPo != null) {
            vo.setContent(contentPo.getContent());
            vo.setSummary(contentPo.getSummary());
            vo.setHighlights(contentPo.getHighlights());
        }

        return vo;
    }

    @Override
    public int deleteById(Long id) {
        return reportMapper.deleteById(id);
    }

    /**
     * 根据分析维度和时间范围获取原始数据（文本格式）
     */
    private String fetchRawData(String analysisDimension, String startDate, String endDate) {
        if ("user_satisfaction".equals(analysisDimension)) {
            // 获取交互文本（优先使用时间范围，否则取最新）
            List<String> interactions;
            if (startDate != null && endDate != null) {
                interactions = reportDataMapper.getCleanedInteractionsByTime(startDate, endDate);
            } else {
                interactions = reportDataMapper.getLatestCleanedInteractions();
            }

            if (interactions == null || interactions.isEmpty()) {
                return "没有符合条件的交互记录。";
            }

            // 简单统计：总条数，并返回前10条文本示例
            int total = interactions.size();
            String samples = interactions.stream().limit(10).collect(Collectors.joining("\n- "));
            return String.format("用户交互反馈（共%d条）：\n- %s", total, samples);
        }

        if ("task_completion_rate".equals(analysisDimension)) {
            List<Map<String, Object>> taskStats = reportDataMapper.getTaskStatistics();
            if (taskStats.isEmpty()) {
                return "没有任务数据。";
            }
            StringBuilder sb = new StringBuilder("任务完成情况统计：\n");
            for (Map<String, Object> stat : taskStats) {
                int status = (Integer) stat.get("status");
                long count = (Long) stat.get("count");
                String statusDesc = getTaskStatusDesc(status);
                sb.append(String.format("- %s：%d 条\n", statusDesc, count));
            }
            return sb.toString();
        }

        if ("exception_rate".equals(analysisDimension)) {
            List<Map<String, Object>> warnStats = reportDataMapper.getWarningStatistics(startDate, endDate);
            if (warnStats.isEmpty()) {
                return "时间范围内无告警记录。";
            }
            StringBuilder sb = new StringBuilder("机器人告警统计：\n");
            for (Map<String, Object> stat : warnStats) {
                int warnType = (Integer) stat.get("warning_type");
                int warnLevel = (Integer) stat.get("warning_level");
                long count = (Long) stat.get("count");
                String typeDesc = getWarningTypeDesc(warnType);
                String levelDesc = getWarningLevelDesc(warnLevel);
                sb.append(String.format("- %s（%s级）：%d 次\n", typeDesc, levelDesc, count));
            }
            return sb.toString();
        }

        // 其他维度可继续扩展
        return "暂不支持该分析维度，请联系管理员。";
    }

    // 辅助方法：任务状态描述
    private String getTaskStatusDesc(int status) {
        switch (status) {
            case 0: return "待执行";
            case 1: return "执行中";
            case 2: return "已完成";
            case 3: return "已暂停";
            case 4: return "已取消";
            case 5: return "失败";
            case 6: return "已结束";
            default: return "未知状态";
        }
    }

    // 辅助方法：告警类型描述
    private String getWarningTypeDesc(int type) {
        switch (type) {
            case 0: return "低电量";
            case 1: return "硬件故障";
            case 2: return "硬件异常";
            case 3: return "离线";
            default: return "未知类型";
        }
    }

    // 辅助方法：告警级别描述
    private String getWarningLevelDesc(int level) {
        switch (level) {
            case 0: return "提示";
            case 1: return "警告";
            case 2: return "错误";
            default: return "未知级别";
        }
    }
}