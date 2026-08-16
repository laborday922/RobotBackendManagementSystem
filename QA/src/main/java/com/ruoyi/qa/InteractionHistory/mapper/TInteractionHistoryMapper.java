package com.ruoyi.qa.InteractionHistory.mapper;

import com.ruoyi.qa.InteractionHistory.domain.TInteractionHistory;

/**
 * 交互历史记录Mapper接口
 *
 * @author xiaocai
 * @date 2026-03-13
 */
public interface TInteractionHistoryMapper
{
    /**
     * 新增交互历史记录
     *
     * @param tInteractionHistory 交互历史记录
     * @return 结果
     */
    public int insertTInteractionHistory(TInteractionHistory tInteractionHistory);
}
