package com.ruoyi.mode.mapper;

import com.ruoyi.mode.domain.SysModeHistory;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 模式切换历史记录Mapper接口
 *
 * @author ruoyi
 */
public interface SysModeHistoryMapper
{

    public SysModeHistory selectSysModeHistoryById(@Param("sysModeHistory") SysModeHistory sysModeHistory);


    public List<SysModeHistory> selectSysModeHistoryList(@Param("sysModeHistory") SysModeHistory sysModeHistory);


    public List<SysModeHistory> selectSysModeHistoryListByTypes(@Param("types") String[] types,
                                                                @Param("history") SysModeHistory history);

    public int insertSysModeHistory(SysModeHistory sysModeHistory);


    public int deleteSysModeHistoryById(Long historyId);

    public int deleteSysModeHistoryByIds(Long[] historyIds);


    public List<SysModeHistory> selectRecentHistory(@Param("limit") Integer limit,
                                                    @Param("tenantId") Long tenantId);


    public List<SysModeHistory> selectHistoryByOperatorAndType(@Param("operator") String operator,
                                                               @Param("operationType") String operationType,
                                                               @Param("tenantId") Long tenantId);


    @MapKey("status")
    public List<Map<String, Object>> selectOperationTypeStats(@Param("tenantId") Long tenantId);


    @MapKey("status")
    public List<Map<String, Object>> selectRobotOperationStats(@Param("tenantId") Long tenantId);


    @MapKey("status")
    public List<Map<String, Object>> selectDailyOperationStats(@Param("days") Integer days,
                                                               @Param("tenantId") Long tenantId);


    public List<SysModeHistory> selectHistoryByTimeRange(@Param("beginTime") String beginTime,
                                                         @Param("endTime") String endTime,
                                                         @Param("tenantId") Long tenantId);


    public int clearAllHistory();


    public int updateSysModeHistory(SysModeHistory sysModeHistory);


    public int deleteHistoryBefore(@Param("expireTime") String expireTime);
}