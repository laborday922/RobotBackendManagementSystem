package com.ruoyi.qa.ChatManage.service.impl;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.qa.ChatManage.domain.QaChat;
import com.ruoyi.qa.ChatManage.domain.QaRobotChatRel;
import com.ruoyi.qa.ChatManage.mapper.QaChatMapper;
import com.ruoyi.qa.ChatManage.mapper.QaRobotChatRelMapper;
import com.ruoyi.qa.ChatManage.service.IQaRobotChatRelService;
import com.ruoyi.robots.domain.Robot;
import com.ruoyi.robots.service.IRobotsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QaRobotChatRelServiceImpl implements IQaRobotChatRelService
{
    @Autowired
    private QaRobotChatRelMapper qaRobotChatRelMapper;

    @Autowired
    private QaChatMapper qaChatMapper;

    @Autowired
    private IRobotsService robotsService;

    @Override
    public QaRobotChatRel selectQaRobotChatRelByRobotId(Long robotId)
    {
        return qaRobotChatRelMapper.selectQaRobotChatRelByRobotId(robotId);
    }

    @Override
    public List<QaRobotChatRel> selectQaRobotChatRelList(QaRobotChatRel qaRobotChatRel)
    {
        return qaRobotChatRelMapper.selectQaRobotChatRelList(qaRobotChatRel);
    }

    @Override
    public int insertQaRobotChatRel(QaRobotChatRel qaRobotChatRel)
    {
        validateQaRobotChatRel(qaRobotChatRel);
        QaRobotChatRel existed = qaRobotChatRelMapper.selectQaRobotChatRelByRobotId(qaRobotChatRel.getRobotId());
        if (existed != null)
        {
            return qaRobotChatRelMapper.updateQaRobotChatRel(qaRobotChatRel);
        }
        return qaRobotChatRelMapper.insertQaRobotChatRel(qaRobotChatRel);
    }

    @Override
    public int updateQaRobotChatRel(QaRobotChatRel qaRobotChatRel)
    {
        validateQaRobotChatRel(qaRobotChatRel);
        QaRobotChatRel existed = qaRobotChatRelMapper.selectQaRobotChatRelByRobotId(qaRobotChatRel.getRobotId());
        if (existed == null)
        {
            return qaRobotChatRelMapper.insertQaRobotChatRel(qaRobotChatRel);
        }
        return qaRobotChatRelMapper.updateQaRobotChatRel(qaRobotChatRel);
    }

    @Override
    public int deleteQaRobotChatRelByRobotIds(Long[] robotIds)
    {
        return qaRobotChatRelMapper.deleteQaRobotChatRelByRobotIds(robotIds);
    }

    @Override
    public QaChat selectQaChatByRobotId(Long robotId)
    {
        return qaRobotChatRelMapper.selectQaChatByRobotId(robotId);
    }

    private void validateQaRobotChatRel(QaRobotChatRel qaRobotChatRel)
    {
        if (qaRobotChatRel == null)
        {
            throw new ServiceException("机器人问答关系不能为空");
        }
        if (qaRobotChatRel.getRobotId() == null)
        {
            throw new ServiceException("机器人不能为空");
        }
        if (qaRobotChatRel.getChatId() == null)
        {
            throw new ServiceException("问答不能为空");
        }

        Robot robot = robotsService.selectRobotsById(qaRobotChatRel.getRobotId());
        if (robot == null)
        {
            throw new ServiceException("机器人不存在");
        }

        QaChat qaChat = qaChatMapper.selectQaChatById(qaRobotChatRel.getChatId());
        if (qaChat == null)
        {
            throw new ServiceException("问答配置不存在");
        }
    }
}
