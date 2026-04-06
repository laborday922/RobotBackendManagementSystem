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
    CleanRulePo selectById(@Param("id") Long id,
                           @Param("tenantId") Long tenantId);

    /**
     * 系统表，不需要 tenant
     */
    List<String> getTableColumns(@Param("tableName") String tableName);

    /**
     * 查询定时任务（按租户隔离）
     */
    List<CleanRulePo> selectScheduledRules(@Param("tenantId") Long tenantId);

    /**
     * 更新运行时间（必须防串租户）
     */
    int updateRuntime(@Param("id") Long id,
                      @Param("runTime") LocalDateTime runTime,
                      @Param("tenantId") Long tenantId);

    /**
     * 批量插入（tenant 在对象里）
     */
    void batchInsertResults(List<CleanResultPo> list);

    /**
     * 查询原始数据（建议加 tenant）
     */
    List<Map<String, Object>> selectRawInteractionData(@Param("tenantId") Long tenantId);
}