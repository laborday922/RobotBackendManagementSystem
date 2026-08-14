package com.ruoyi.taskmgt.service;

import java.util.Date;
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

    /**
     * 根据 interactionId 从 TaskLog 构建交互历史记录并保存
     *
     * @param interactionId 交互唯一标识
     * @param rating        评分
     * @param evaluationText 评价文本
     * @return 保存的记录条数
     */
    int buildAndSaveEvaluation(String interactionId, Long rating, String evaluationText);

    /**
     * 保存问答评价记录（来源类型固定为 2-问答，与任务无关）
     *
     * @param rating         评分
     * @param evaluationText 评价文本
     * @param interactionTime 交互发生时间
     * @param duration       交互耗时（秒）
     * @return 保存的记录条数
     */
    int saveQAEvaluation(Long rating, String evaluationText, Date interactionTime, Long duration);
}
