package com.ruoyi.qa.ChatManage.service.impl;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.qa.ChatManage.domain.QaChat;
import com.ruoyi.qa.ChatManage.mapper.QaChatMapper;
import com.ruoyi.qa.ChatManage.mapper.QaRobotChatRelMapper;
import com.ruoyi.qa.ChatManage.service.IQaChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class QaChatServiceImpl implements IQaChatService
{
    @Autowired
    private QaChatMapper qaChatMapper;

    @Autowired
    private QaRobotChatRelMapper qaRobotChatRelMapper;

    @Override
    public QaChat selectQaChatById(Long id)
    {
        return qaChatMapper.selectQaChatById(id);
    }

    @Override
    public List<QaChat> selectQaChatList(QaChat qaChat)
    {
        return qaChatMapper.selectQaChatList(qaChat);
    }

    @Override
    public List<QaChat> selectQaChatOptions()
    {
        return qaChatMapper.selectQaChatOptions();
    }

    @Override
    public int insertQaChat(QaChat qaChat)
    {
        validateQaChat(qaChat);
        return qaChatMapper.insertQaChat(qaChat);
    }

    @Override
    public int updateQaChat(QaChat qaChat)
    {
        if (qaChat == null || qaChat.getId() == null)
        {
            throw new ServiceException("问答ID不能为空");
        }
        validateQaChat(qaChat);
        return qaChatMapper.updateQaChat(qaChat);
    }

    @Override
    public int deleteQaChatByIds(Long[] ids)
    {
        if (ids == null || ids.length == 0)
        {
            return 0;
        }
        if (qaRobotChatRelMapper.countByChatIds(ids) > 0)
        {
            throw new ServiceException("存在已绑定机器人的问答，请先解除绑定");
        }
        return qaChatMapper.deleteQaChatByIds(ids);
    }

    private void validateQaChat(QaChat qaChat)
    {
        if (qaChat == null)
        {
            throw new ServiceException("问答配置不能为空");
        }
        if (!StringUtils.hasText(qaChat.getChatName()))
        {
            throw new ServiceException("问答名称不能为空");
        }
        if (!StringUtils.hasText(qaChat.getDifyApiKey()))
        {
            throw new ServiceException("Dify应用Key不能为空");
        }
        qaChat.setChatName(qaChat.getChatName().trim());
        qaChat.setDifyApiKey(qaChat.getDifyApiKey().trim());
        if (qaChat.getChatDesc() != null)
        {
            qaChat.setChatDesc(qaChat.getChatDesc().trim());
        }
    }
}
