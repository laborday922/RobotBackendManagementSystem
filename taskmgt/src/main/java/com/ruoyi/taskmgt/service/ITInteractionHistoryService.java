package com.ruoyi.taskmgt.service;

import java.util.List;

import com.ruoyi.taskmgt.domain.vo.SumOfInteractionHistoryVo;
import com.ruoyi.taskmgt.domain.TInteractionHistory;

/**
 * 交互历史记录Service接口
 * 
 * @author xiaocai
 * @date 2026-03-13
 */
public interface ITInteractionHistoryService 
{
    /**
     * 查询交互历史记录
     * 
     * @param id 交互历史记录主键
     * @return 交互历史记录
     */
    public TInteractionHistory selectTInteractionHistoryById(Long id);

    /**
     * 查询交互历史记录列表
     * 
     * @param tInteractionHistory 交互历史记录
     * @return 交互历史记录集合
     */
    public List<TInteractionHistory> selectTInteractionHistoryList(TInteractionHistory tInteractionHistory);

    /**
     * 新增交互历史记录
     * 
     * @param tInteractionHistory 交互历史记录
     * @return 结果
     */
    public int insertTInteractionHistory(TInteractionHistory tInteractionHistory);

    /**
     * 修改交互历史记录
     * 
     * @param tInteractionHistory 交互历史记录
     * @return 结果
     */
    public int updateTInteractionHistory(TInteractionHistory tInteractionHistory);

    /**
     * 批量删除交互历史记录
     * 
     * @param ids 需要删除的交互历史记录主键集合
     * @return 结果
     */
    public int deleteTInteractionHistoryByIds(Long[] ids);

    /**
     * 删除交互历史记录信息
     * 
     * @param id 交互历史记录主键
     * @return 结果
     */
    public int deleteTInteractionHistoryById(Long id);

    SumOfInteractionHistoryVo sumOfInteractionHistory();
}
