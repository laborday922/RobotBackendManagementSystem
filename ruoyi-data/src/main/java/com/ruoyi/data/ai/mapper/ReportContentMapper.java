package com.ruoyi.data.ai.mapper;

import com.ruoyi.data.ai.mapper.po.ReportContentPo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ReportContentMapper {

    int insertContent(ReportContentPo content);

    //报告文件下载
    ReportContentPo selectContentByReportId(Long reportId);
}
