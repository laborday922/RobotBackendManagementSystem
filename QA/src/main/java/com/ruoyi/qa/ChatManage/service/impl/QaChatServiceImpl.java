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
        QaChat db = qaChatMapper.selectQaChatById(qaChat.getId());
        if (db == null)
        {
            throw new ServiceException("问答配置不存在");
        }
        validateQaChatForUpdate(qaChat, db);
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
        if (!StringUtils.hasText(qaChat.getChatType()))
        {
            throw new ServiceException("对话类型不能为空");
        }
        if (!StringUtils.hasText(qaChat.getApiKey()))
        {
            throw new ServiceException("API Key不能为空");
        }

        qaChat.setChatName(qaChat.getChatName().trim());
        qaChat.setChatType(qaChat.getChatType().trim().toLowerCase());
        qaChat.setApiKey(qaChat.getApiKey().trim());
        if (qaChat.getChatDesc() != null)
        {
            qaChat.setChatDesc(qaChat.getChatDesc().trim());
        }
        if (qaChat.getBaseUrl() != null)
        {
            qaChat.setBaseUrl(qaChat.getBaseUrl().trim());
        }
        if (qaChat.getModelName() != null)
        {
            qaChat.setModelName(qaChat.getModelName().trim());
        }

        validateChatType(qaChat);
    }

    private void validateQaChatForUpdate(QaChat qaChat, QaChat db)
    {
        if (qaChat == null)
        {
            throw new ServiceException("问答配置不能为空");
        }
        if (!StringUtils.hasText(qaChat.getChatName()))
        {
            throw new ServiceException("问答名称不能为空");
        }

        qaChat.setChatName(qaChat.getChatName().trim());
        if (qaChat.getChatDesc() != null)
        {
            qaChat.setChatDesc(qaChat.getChatDesc().trim());
        }
        if (StringUtils.hasText(qaChat.getChatType()))
        {
            qaChat.setChatType(qaChat.getChatType().trim().toLowerCase());
        }
        else
        {
            qaChat.setChatType(db.getChatType());
        }
        if (StringUtils.hasText(qaChat.getApiKey()))
        {
            qaChat.setApiKey(qaChat.getApiKey().trim());
        }
        else
        {
            qaChat.setApiKey(db.getApiKey());
        }
        if (qaChat.getBaseUrl() != null)
        {
            qaChat.setBaseUrl(qaChat.getBaseUrl().trim());
        }
        else
        {
            qaChat.setBaseUrl(db.getBaseUrl());
        }
        if (qaChat.getModelName() != null)
        {
            qaChat.setModelName(qaChat.getModelName().trim());
        }
        else
        {
            qaChat.setModelName(db.getModelName());
        }

        validateChatType(qaChat);
    }

    private void validateChatType(QaChat qaChat)
    {
        String chatType = qaChat.getChatType();
        if (!"dify".equals(chatType) && !"openai".equals(chatType))
        {
            throw new ServiceException("对话类型仅支持 dify 或 openai");
        }
        if ("openai".equals(chatType))
        {
            if (!StringUtils.hasText(qaChat.getBaseUrl()))
            {
                throw new ServiceException("OpenAI 类型必须配置接口地址");
            }
            if (!StringUtils.hasText(qaChat.getModelName()))
            {
                throw new ServiceException("OpenAI 类型必须配置模型名称");
            }
        }
    }
}
