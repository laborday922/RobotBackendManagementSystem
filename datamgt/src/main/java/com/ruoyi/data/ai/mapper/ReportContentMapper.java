package com.ruoyi.data.ai.mapper;

import com.ruoyi.data.ai.mapper.po.ReportContentPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ReportContentMapper {

    int insertContent(ReportContentPo content);

    //报告文件下载
    ReportContentPo selectContentByReportId(@Param("reportId") Long reportId,
                                            @Param("tenantId") Long tenantId);
}
