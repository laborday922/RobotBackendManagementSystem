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
    /**
     * 查询历史记录
     *
     * @param historyId 历史ID
     * @return 历史记录
     */
    public SysModeHistory selectSysModeHistoryById(Long historyId);

    /**
     * 查询历史记录列表
     *
     * @param sysModeHistory 历史记录
     * @return 历史记录集合
     */
    public List<SysModeHistory> selectSysModeHistoryList(SysModeHistory sysModeHistory);

    /**
     * 根据多个操作类型查询历史记录列表
     *
     * @param types 操作类型数组
     * @param history 历史记录查询条件
     * @return 历史记录集合
     */
    public List<SysModeHistory> selectSysModeHistoryListByTypes(@Param("types") String[] types,
                                                                @Param("history") SysModeHistory history);

    /**
     * 新增历史记录
     *
     * @param sysModeHistory 历史记录
     * @return 结果
     */
    public int insertSysModeHistory(SysModeHistory sysModeHistory);

    /**
     * 删除历史记录
     *
     * @param historyId 历史ID
     * @return 结果
     */
    public int deleteSysModeHistoryById(Long historyId);

    /**
     * 批量删除历史记录
     *
     * @param historyIds 需要删除的数据ID
     * @return 结果
     */
    public int deleteSysModeHistoryByIds(Long[] historyIds);

    /**
     * 获取最近的操作记录
     *
     * @param limit 限制数量
     * @return 历史记录集合
     */
    public List<SysModeHistory> selectRecentHistory(Integer limit);

    /**
     * 根据操作人和操作类型查询历史记录
     *
     * @param operator 操作人
     * @param operationType 操作类型
     * @return 历史记录集合
     */
    public List<SysModeHistory> selectHistoryByOperatorAndType(@Param("operator") String operator,
                                                               @Param("operationType") String operationType);

    /**
     * 统计各类型操作的数量
     *
     * @return 统计结果
     */
    @MapKey("status")
    public List<Map<String, Object>> selectOperationTypeStats();

    /**
     * 统计各机器人的操作次数
     *
     * @return 统计结果
     */
    @MapKey("status")
    public List<Map<String, Object>> selectRobotOperationStats();

    /**
     * 统计每天的操作数量
     *
     * @param days 最近几天
     * @return 统计结果
     */
    @MapKey("status")
    public List<Map<String, Object>> selectDailyOperationStats(Integer days);

    /**
     * 根据时间范围查询历史记录
     *
     * @param beginTime 开始时间
     * @param endTime 结束时间
     * @return 历史记录集合
     */
    public List<SysModeHistory> selectHistoryByTimeRange(@Param("beginTime") String beginTime,
                                                         @Param("endTime") String endTime);

    /**
     * 清空所有历史记录
     *
     * @return 结果
     */
    public int clearAllHistory();

    /**
     * 修改历史记录
     *
     * @param sysModeHistory 历史记录
     * @return 结果
     */
    public int updateSysModeHistory(SysModeHistory sysModeHistory);
}