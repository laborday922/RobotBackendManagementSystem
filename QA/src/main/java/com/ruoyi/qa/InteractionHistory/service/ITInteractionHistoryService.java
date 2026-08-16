package com.ruoyi.qa.InteractionHistory.service;

import java.util.Date;

/**
 * 交互历史记录Service接口
 *
 * @author xiaocai
 * @date 2026-03-13
 */
public interface ITInteractionHistoryService
{
    /**
     * 保存问答评价记录（来源类型固定为 2-问答，与任务无关）
     *
     * @param robotId        机器人ID
     * @param rating         评分
     * @param evaluationText 评价文本
     * @param interactionTime 交互发生时间
     * @param duration       交互耗时（秒）
     * @return 保存的记录条数
     */
    int saveQAEvaluation(Long robotId, Long rating, String evaluationText, Date interactionTime, Long duration);
}
