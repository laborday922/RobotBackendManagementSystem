package com.ruoyi.qa.InteractionHistory.service.impl;

import java.util.Date;

import com.ruoyi.qa.ChatManage.domain.QaChat;
import com.ruoyi.qa.ChatManage.service.IQaRobotChatRelService;
import com.ruoyi.qa.InteractionHistory.service.IQaInteractionHistoryService;
import com.ruoyi.taskmgt.domain.TInteractionHistory;
import com.ruoyi.taskmgt.mapper.TInteractionHistoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 问答评价Service业务层处理
 *
 * @author xiaocai
 * @date 2026-03-13
 */
@Service
public class QaInteractionHistoryServiceImpl implements IQaInteractionHistoryService
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
        record.setInteractionContent(StringUtils.hasText(chatName) ? "问答：" + chatName : "问答");
        record.setRating(rating);
        record.setEvaluationText(evaluationText);
        record.setInteractionTime(interactionTime);
        record.setDuration(duration);
        record.setStatus(0L); // 0-成功

        return tInteractionHistoryMapper.insertTInteractionHistory(record);
    }
}
