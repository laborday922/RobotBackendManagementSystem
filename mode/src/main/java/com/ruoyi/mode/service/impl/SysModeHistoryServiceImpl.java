package com.ruoyi.mode.service.impl;

import com.ruoyi.common.threadlocal.TenantContext;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.mode.domain.SysModeHistory;
import com.ruoyi.mode.mapper.SysModeHistoryMapper;
import com.ruoyi.mode.service.ISysModeHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 模式切换历史记录Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class SysModeHistoryServiceImpl implements ISysModeHistoryService
{
    @Autowired
    private SysModeHistoryMapper sysModeHistoryMapper;

    /**
     * 获取当前租户ID
     */
    private Long getCurrentTenantId() {
        Long tenantId = TenantContext.get();
        return tenantId == null ? 0L : tenantId;
    }

    /**
     * 查询历史记录
     *
     * @param historyId 历史ID
     * @return 历史记录
     */
    @Override
    public SysModeHistory selectSysModeHistoryById(Long historyId)
    {
        SysModeHistory query = new SysModeHistory();
        query.setHistoryId(historyId);
        query.setTenantId(getCurrentTenantId());
        return sysModeHistoryMapper.selectSysModeHistoryById(query);
    }

    /**
     * 查询历史记录列表
     *
     * @param sysModeHistory 历史记录
     * @return 历史记录
     */
    @Override
    public List<SysModeHistory> selectSysModeHistoryList(SysModeHistory sysModeHistory)
    {
        if (sysModeHistory == null) {
            sysModeHistory = new SysModeHistory();
        }
        sysModeHistory.setTenantId(getCurrentTenantId());

        // 如果 operationType 包含逗号，说明是多个类型
        if (sysModeHistory.getOperationType() != null &&
                sysModeHistory.getOperationType().contains(",")) {
            String[] types = sysModeHistory.getOperationType().split(",");
            return sysModeHistoryMapper.selectSysModeHistoryListByTypes(types, sysModeHistory);
        }
        return sysModeHistoryMapper.selectSysModeHistoryList(sysModeHistory);
    }

    /**
     * 新增历史记录
     *
     * @param sysModeHistory 历史记录
     * @return 结果
     */
    @Override
    @Transactional
    public int insertSysModeHistory(SysModeHistory sysModeHistory)
    {
        sysModeHistory.setCreateTime(DateUtils.getNowDate());
        sysModeHistory.setTenantId(getCurrentTenantId());

        if (sysModeHistory.getOperationTime() == null) {
            sysModeHistory.setOperationTime(DateUtils.getNowDate());
        }

        return sysModeHistoryMapper.insertSysModeHistory(sysModeHistory);
    }

    /**
     * 修改历史记录
     *
     * @param sysModeHistory 历史记录
     * @return 结果
     */
    @Override
    @Transactional
    public int updateSysModeHistory(SysModeHistory sysModeHistory)
    {
        sysModeHistory.setUpdateTime(DateUtils.getNowDate());
        return sysModeHistoryMapper.updateSysModeHistory(sysModeHistory);
    }

    /**
     * 批量删除历史记录
     *
     * @param historyIds 需要删除的历史记录ID
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteSysModeHistoryByIds(Long[] historyIds)
    {
        return sysModeHistoryMapper.deleteSysModeHistoryByIds(historyIds);
    }

    /**
     * 删除历史记录信息
     *
     * @param historyId 历史记录ID
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteSysModeHistoryById(Long historyId)
    {
        return sysModeHistoryMapper.deleteSysModeHistoryById(historyId);
    }

    /**
     * 清空所有历史记录
     *
     * @return 结果
     */
    @Override
    @Transactional
    public int clearAllHistory()
    {
        return sysModeHistoryMapper.clearAllHistory();
    }

    /**
     * 根据操作类型查询历史记录
     *
     * @param operationType 操作类型
     * @return 历史记录集合
     */
    @Override
    public List<SysModeHistory> selectHistoryByType(String operationType)
    {
        SysModeHistory history = new SysModeHistory();
        history.setOperationType(operationType);
        history.setTenantId(getCurrentTenantId());
        return sysModeHistoryMapper.selectSysModeHistoryList(history);
    }

    /**
     * 根据机器人ID查询历史记录
     *
     * @param robotId 机器人ID
     * @return 历史记录集合
     */
    @Override
    public List<SysModeHistory> selectHistoryByRobotId(Long robotId)
    {
        SysModeHistory history = new SysModeHistory();
        history.setRobotId(robotId);
        history.setTenantId(getCurrentTenantId());
        return sysModeHistoryMapper.selectSysModeHistoryList(history);
    }

    /**
     * 根据时间范围查询历史记录
     *
     * @param beginTime 开始时间
     * @param endTime 结束时间
     * @return 历史记录集合
     */
    @Override
    public List<SysModeHistory> selectHistoryByTimeRange(String beginTime, String endTime)
    {
        Long tenantId = getCurrentTenantId();
        return sysModeHistoryMapper.selectHistoryByTimeRange(beginTime, endTime, tenantId);
    }

    /**
     * 获取最近的操作记录
     *
     * @param limit 限制数量
     * @return 历史记录集合
     */
    @Override
    public List<SysModeHistory> selectRecentHistory(Integer limit)
    {
        Long tenantId = getCurrentTenantId();
        return sysModeHistoryMapper.selectRecentHistory(limit, tenantId);
    }

    /**
     * 根据操作人和操作类型查询历史记录
     *
     * @param operator 操作人
     * @param operationType 操作类型
     * @return 历史记录集合
     */
    @Override
    public List<SysModeHistory> selectHistoryByOperatorAndType(String operator, String operationType)
    {
        Long tenantId = getCurrentTenantId();
        return sysModeHistoryMapper.selectHistoryByOperatorAndType(operator, operationType, tenantId);
    }

    /**
     * 统计各类型操作的数量
     *
     * @return 统计结果
     */
    @Override
    public List<Map<String, Object>> selectOperationTypeStats()
    {
        Long tenantId = getCurrentTenantId();
        return sysModeHistoryMapper.selectOperationTypeStats(tenantId);
    }

    /**
     * 统计各机器人的操作次数
     *
     * @return 统计结果
     */
    @Override
    public List<Map<String, Object>> selectRobotOperationStats()
    {
        Long tenantId = getCurrentTenantId();
        return sysModeHistoryMapper.selectRobotOperationStats(tenantId);
    }

    /**
     * 统计每天的操作数量
     *
     * @param days 最近几天
     * @return 统计结果
     */
    @Override
    public List<Map<String, Object>> selectDailyOperationStats(Integer days)
    {
        Long tenantId = getCurrentTenantId();
        return sysModeHistoryMapper.selectDailyOperationStats(days, tenantId);
    }

    /**
     * 删除指定时间之前的记录
     *
     * @param expireTime 过期时间
     * @return 结果
     */
    public int deleteHistoryBefore(String expireTime)
    {
        return sysModeHistoryMapper.deleteHistoryBefore(expireTime);
    }
}