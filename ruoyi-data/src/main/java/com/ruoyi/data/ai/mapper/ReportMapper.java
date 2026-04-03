package com.ruoyi.data.ai.mapper;

import com.ruoyi.data.ai.controller.dto.ReportQueryDto;
import com.ruoyi.data.ai.mapper.po.ReportPo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ReportMapper {

    int insertReport(ReportPo report);

    List<ReportPo> selectReportList(ReportQueryDto query);

    //报告文档下载
    ReportPo selectById(Long id);

    int deleteById(Long id);
}