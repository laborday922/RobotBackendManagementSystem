package com.ruoyi.data.ai.mapper;

import com.ruoyi.data.ai.mapper.po.ReportContentPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ReportContentMapper {

    int insertContent(ReportContentPo content);

    int updateContent(ReportContentPo content);

    int deleteContentByReportId(@Param("reportId") Long reportId);

    //报告文件下载
    ReportContentPo selectContentByReportId(@Param("reportId") Long reportId);
}
