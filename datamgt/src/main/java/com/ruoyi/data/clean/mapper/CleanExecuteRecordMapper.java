package com.ruoyi.data.clean.mapper;

import com.ruoyi.data.clean.domain.CleanExecuteRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CleanExecuteRecordMapper {

    int insert(CleanExecuteRecord record);

    List<CleanExecuteRecord> selectAll(@Param("tenantId") Long tenantId);
}
