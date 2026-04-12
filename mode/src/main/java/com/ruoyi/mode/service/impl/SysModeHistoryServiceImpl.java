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

import static com.ruoyi.common.utils.SecurityUtils.isAdmin;

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
     * 查询历史记录
     *
     * @param historyId 历史ID
     * @return 历史记录
     */
    @Override
    public SysModeHistory selectSysModeHistoryById(Long historyId)
    {
        return sysModeHistoryMapper.selectSysModeHistoryById(historyId);
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
        // 设置租户过滤
        Long tenantId = TenantContext.get();
        if(!isAdmin(tenantId))
            sysModeHistory.setTenantId(tenantId);

        // 将开始日期添加时间部分 "00:00:00"
        if (sysModeHistory.getBeginTime() != null && !sysModeHistory.getBeginTime().isEmpty()) {
            // 如果日期已经包含时间部分，不重复添加
            if (!sysModeHistory.getBeginTime().contains(":")) {
                sysModeHistory.setBeginTime(sysModeHistory.getBeginTime() + " 00:00:00");
            }
        }
        // 将结束日期添加时间部分 "23:59:59"
        if (sysModeHistory.getEndTime() != null && !sysModeHistory.getEndTime().isEmpty()) {
            if (!sysModeHistory.getEndTime().contains(":")) {
                sysModeHistory.setEndTime(sysModeHistory.getEndTime() + " 23:59:59");
            }
        }

        // 如果 operationType 包含逗号，说明是多个类型
        if (sysModeHistory.getOperationType() != null &&
                sysModeHistory.getOperationType().contains(",")) {
            // 分割字符串，转换为数组
            String[] types = sysModeHistory.getOperationType().split(",");
            // 调用新的 Mapper 方法
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
        // 设置租户ID
        sysModeHistory.setTenantId(TenantContext.get());
        // 设置创建时间
        sysModeHistory.setCreateTime(DateUtils.getNowDate());

        // 如果操作时间为空，设置为当前时间
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
        Long tenantId = TenantContext.get();
        if(!isAdmin(tenantId))
            history.setTenantId(tenantId);
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
        Long tenantId = TenantContext.get();
        if(!isAdmin(tenantId))
            history.setTenantId(tenantId);
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
        // 修复：添加时间部分
        if (beginTime != null && !beginTime.isEmpty() && !beginTime.contains(":")) {
            beginTime = beginTime + " 00:00:00";
        }
        if (endTime != null && !endTime.isEmpty() && !endTime.contains(":")) {
            endTime = endTime + " 23:59:59";
        }
        return sysModeHistoryMapper.selectHistoryByTimeRange(beginTime, endTime);
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
        Long tenantId = TenantContext.get();
        return sysModeHistoryMapper.selectRecentHistory(limit, isAdmin(tenantId) ? null : tenantId);
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
        Long tenantId = TenantContext.get();
        return sysModeHistoryMapper.selectHistoryByOperatorAndType(operator, operationType, isAdmin(tenantId) ? null : tenantId);
    }

    /**
     * 统计各类型操作的数量
     *
     * @return 统计结果
     */
    @Override
    public List<Map<String, Object>> selectOperationTypeStats()
    {
        Long tenantId = TenantContext.get();
        return sysModeHistoryMapper.selectOperationTypeStats(isAdmin(tenantId) ? null : tenantId);
    }

    /**
     * 统计各机器人的操作次数
     *
     * @return 统计结果
     */
    @Override
    public List<Map<String, Object>> selectRobotOperationStats()
    {
        Long tenantId = TenantContext.get();
        return sysModeHistoryMapper.selectRobotOperationStats(isAdmin(tenantId) ? null : tenantId);
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
        Long tenantId = TenantContext.get();
        return sysModeHistoryMapper.selectDailyOperationStats(days, isAdmin(tenantId) ? null : tenantId);
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