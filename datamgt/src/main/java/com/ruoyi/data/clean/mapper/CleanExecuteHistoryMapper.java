package com.ruoyi.data.clean.mapper;

import com.ruoyi.data.clean.domain.CleanExecuteHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CleanExecuteHistoryMapper {

    int insert(CleanExecuteHistory history);

    CleanExecuteHistory selectById(@Param("id") Long id,
                                   @Param("tenantId") Long tenantId);

    List<CleanExecuteHistory> selectAll(@Param("tenantId") Long tenantId);

    int updateStatus(@Param("id") Long id,
                     @Param("status") Integer status,
                     @Param("tenantId") Long tenantId);

    int deleteById(@Param("id") Long id,
                   @Param("tenantId") Long tenantId);
}