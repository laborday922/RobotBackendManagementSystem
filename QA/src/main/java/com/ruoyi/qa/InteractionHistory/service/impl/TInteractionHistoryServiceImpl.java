package com.ruoyi.qa.InteractionHistory.service.impl;

import java.util.Date;

import com.ruoyi.qa.ChatManage.domain.QaChat;
import com.ruoyi.qa.ChatManage.service.IQaRobotChatRelService;
import com.ruoyi.qa.InteractionHistory.domain.TInteractionHistory;
import com.ruoyi.qa.InteractionHistory.mapper.TInteractionHistoryMapper;
import com.ruoyi.qa.InteractionHistory.service.ITInteractionHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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

    @Autowired
    private IQaRobotChatRelService qaRobotChatRelService;

    @Override
    public int saveQAEvaluation(Long robotId, Long rating, String evaluationText, Date interactionTime, Long duration)
    {
        QaChat qaChat = qaRobotChatRelService.selectQaChatByRobotId(robotId);
        String chatName = qaChat != null ? qaChat.getChatName() : null;

        TInteractionHistory record = new TInteractionHistory();
        record.setRobotId(robotId);
        record.setSourceType(2L); // 2-问答
        record.setInteractionContent(StringUtils.hasText(chatName) ? "问答：" + chatName : "机器人未配置问答");
        record.setRating(rating);
        record.setEvaluationText(evaluationText);
        record.setInteractionTime(interactionTime);
        record.setDuration(duration);
        record.setStatus(0L); // 0-成功

        return tInteractionHistoryMapper.insertTInteractionHistory(record);
    }
}
