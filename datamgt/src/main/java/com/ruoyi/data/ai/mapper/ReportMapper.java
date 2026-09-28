package com.ruoyi.data.ai.mapper;

import com.ruoyi.data.ai.controller.dto.ReportQueryDto;
import com.ruoyi.data.ai.mapper.po.ReportPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ReportMapper {

    int insertReport(ReportPo report);

    List<ReportPo> selectReportList(ReportQueryDto query);

    ReportPo selectById(@Param("id") Long id);

    int deleteById(@Param("id") Long id);
}