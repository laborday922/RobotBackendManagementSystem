package com.ruoyi.data.ai.mapper;

import com.ruoyi.data.ai.controller.dto.ReportQueryDto;
import com.ruoyi.data.ai.mapper.po.ReportPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ReportMapper {

    int insertReport(ReportPo report); // tenantId 放在 report 里

    List<ReportPo> selectReportList(ReportQueryDto query);
    // 👉 query 里要加 tenantId

    ReportPo selectById(@Param("id") Long id,
                        @Param("tenantId") Long tenantId);

    int deleteById(@Param("id") Long id,
                   @Param("tenantId") Long tenantId);
}