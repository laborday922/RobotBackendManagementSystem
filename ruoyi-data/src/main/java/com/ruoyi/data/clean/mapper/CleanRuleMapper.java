package com.ruoyi.data.clean.mapper;

import com.ruoyi.data.clean.mapper.po.CleanResultPo;
import com.ruoyi.data.clean.mapper.po.CleanRulePo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface CleanRuleMapper {

    /**
     * 根据ID查询任务
     */
    CleanRulePo selectById(@Param("id") Long id);

    List<String> getTableColumns(@Param("tableName") String tableName);

    List<CleanRulePo> selectScheduledRules();

    int updateRuntime(@Param("id") Long id, @Param("runTime") LocalDateTime runTime);

    void batchInsertResults(List<CleanResultPo> list);

    List<Map<String, Object>> selectRawInteractionData();
}