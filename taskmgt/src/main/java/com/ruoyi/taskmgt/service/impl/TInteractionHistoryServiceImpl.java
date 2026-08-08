package com.ruoyi.taskmgt.service.impl;

import java.util.List;

import com.ruoyi.taskmgt.domain.vo.SumOfInteractionHistoryVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.taskmgt.mapper.TInteractionHistoryMapper;
import com.ruoyi.taskmgt.domain.TInteractionHistory;
import com.ruoyi.taskmgt.service.ITInteractionHistoryService;

/**
 * 交互历史记录Service业务层处理
 * 
 * @author xiaocai
 * @date 2026-03-13
 */
@Service
public class TInteractionHistoryServiceImpl implements ITInteractionHistoryService 
{
    @Autowired
    private TInteractionHistoryMapper tInteractionHistoryMapper;

    /**
     * 查询交互历史记录
     * 
     * @param id 交互历史记录主键
     * @return 交互历史记录
     */
    @Override
    public TInteractionHistory selectTInteractionHistoryById(Long id)
    {
        return tInteractionHistoryMapper.selectTInteractionHistoryById(id);
    }

    /**
     * 查询交互历史记录列表
     * 
     * @param tInteractionHistory 交互历史记录
     * @return 交互历史记录
     */
    @Override
    public List<TInteractionHistory> selectTInteractionHistoryList(TInteractionHistory tInteractionHistory)
    {
        return tInteractionHistoryMapper.selectTInteractionHistoryList(tInteractionHistory);
    }

    /**
     * 新增交互历史记录
     * 
     * @param tInteractionHistory 交互历史记录
     * @return 结果
     */
    @Override
    public int insertTInteractionHistory(TInteractionHistory tInteractionHistory)
    {
        return tInteractionHistoryMapper.insertTInteractionHistory(tInteractionHistory);
    }

    /**
     * 修改交互历史记录
     * 
     * @param tInteractionHistory 交互历史记录
     * @return 结果
     */
    @Override
    public int updateTInteractionHistory(TInteractionHistory tInteractionHistory)
    {
        return tInteractionHistoryMapper.updateTInteractionHistory(tInteractionHistory);
    }

    /**
     * 批量删除交互历史记录
     * 
     * @param ids 需要删除的交互历史记录主键
     * @return 结果
     */
    @Override
    public int deleteTInteractionHistoryByIds(Long[] ids)
    {
        return tInteractionHistoryMapper.deleteTInteractionHistoryByIds(ids);
    }

    /**
     * 删除交互历史记录信息
     * 
     * @param id 交互历史记录主键
     * @return 结果
     */
    @Override
    public int deleteTInteractionHistoryById(Long id)
    {
        return tInteractionHistoryMapper.deleteTInteractionHistoryById(id);
    }

    @Override
    public SumOfInteractionHistoryVo sumOfInteractionHistory() {
        SumOfInteractionHistoryVo sumOfInteractionHistoryVo = tInteractionHistoryMapper.sumOfInteractionHistory();
        sumOfInteractionHistoryVo.calculateAverageRating();
        return sumOfInteractionHistoryVo;
    }
}
