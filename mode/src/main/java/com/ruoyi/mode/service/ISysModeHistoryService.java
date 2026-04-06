package com.ruoyi.mode.service;

import com.ruoyi.mode.domain.SysModeHistory;

import java.util.List;
import java.util.Map;

/**
 * 模式切换历史记录Service接口
 *
 * @author ruoyi
 */
public interface ISysModeHistoryService
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
     * 新增历史记录
     *
     * @param sysModeHistory 历史记录
     * @return 结果
     */
    public int insertSysModeHistory(SysModeHistory sysModeHistory);

    /**
     * 修改历史记录
     *
     * @param sysModeHistory 历史记录
     * @return 结果
     */
    public int updateSysModeHistory(SysModeHistory sysModeHistory);

    /**
     * 批量删除历史记录
     *
     * @param historyIds 需要删除的历史记录ID
     * @return 结果
     */
    public int deleteSysModeHistoryByIds(Long[] historyIds);

    /**
     * 删除历史记录信息
     *
     * @param historyId 历史记录ID
     * @return 结果
     */
    public int deleteSysModeHistoryById(Long historyId);

    /**
     * 清空所有历史记录
     *
     * @return 结果
     */
    public int clearAllHistory();

    /**
     * 根据操作类型查询历史记录
     *
     * @param operationType 操作类型
     * @return 历史记录集合
     */
    public List<SysModeHistory> selectHistoryByType(String operationType);

    /**
     * 根据机器人ID查询历史记录
     *
     * @param robotId 机器人ID
     * @return 历史记录集合
     */
    public List<SysModeHistory> selectHistoryByRobotId(Long robotId);

    /**
     * 根据时间范围查询历史记录
     *
     * @param beginTime 开始时间
     * @param endTime 结束时间
     * @return 历史记录集合
     */
    public List<SysModeHistory> selectHistoryByTimeRange(String beginTime, String endTime);

    List<SysModeHistory> selectRecentHistory(Integer limit);

    List<SysModeHistory> selectHistoryByOperatorAndType(String operator, String operationType);

    List<Map<String, Object>> selectOperationTypeStats();

    List<Map<String, Object>> selectRobotOperationStats();

    List<Map<String, Object>> selectDailyOperationStats(Integer days);
}