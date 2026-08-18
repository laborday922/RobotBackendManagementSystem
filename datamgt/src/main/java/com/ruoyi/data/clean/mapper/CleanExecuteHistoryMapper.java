package com.ruoyi.data.clean.mapper;

import com.ruoyi.data.clean.domain.CleanExecuteHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CleanExecuteHistoryMapper {

    int insert(CleanExecuteHistory history);

    CleanExecuteHistory selectById(@Param("id") Long id);

    List<CleanExecuteHistory> selectAll();

    int update(CleanExecuteHistory history);

    int deleteById(@Param("id") Long id);
}