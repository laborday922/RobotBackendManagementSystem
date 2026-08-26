package com.ruoyi.data.clean.mapper;

import com.ruoyi.data.clean.mapper.po.CleanResultPo;
import com.ruoyi.data.clean.mapper.po.CleanRulePo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface CleanRuleMapper {

    /**
     * 根据ID查询任务
     */
    CleanRulePo selectById(@Param("id") Long id);

    List<String> getTableColumns(@Param("tableName") String tableName);

    /**
     * 查询定时任务
     */
    List<CleanRulePo> selectScheduledRules();

    /**
     * 批量插入清洗结果
     */
    void batchInsertResults(List<CleanResultPo> list);

    /**
     * 清空所有历史清洗结果（每次执行前调用，保证只保留最新结果）
     */
    int deleteAllResults();

    /**
     * 查询原始数据
     */
    List<Map<String, Object>> selectRawInteractionData();

    List<Map<String, Object>> selectRawQaLogData();
}
