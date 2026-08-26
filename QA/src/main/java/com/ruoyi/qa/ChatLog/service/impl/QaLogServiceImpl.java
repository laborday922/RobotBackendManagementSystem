package com.ruoyi.qa.ChatLog.service.impl;

import com.ruoyi.qa.ChatLog.domain.QaLog;
import com.ruoyi.qa.ChatLog.mapper.QaLogMapper;
import com.ruoyi.qa.ChatLog.service.IQaLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class QaLogServiceImpl implements IQaLogService
{
    @Autowired
    private QaLogMapper qaLogMapper;

    @Override
    public int insertQaLog(QaLog qaLog)
    {
        if (qaLog == null)
        {
            return 0;
        }
        return qaLogMapper.insertQaLog(qaLog);
    }
}
