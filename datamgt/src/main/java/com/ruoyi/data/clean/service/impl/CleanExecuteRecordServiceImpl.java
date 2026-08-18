package com.ruoyi.data.clean.service.impl;

import com.ruoyi.data.clean.domain.CleanExecuteRecord;
import com.ruoyi.data.clean.mapper.CleanExecuteRecordMapper;
import com.ruoyi.data.clean.service.CleanExecuteRecordService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class CleanExecuteRecordServiceImpl implements CleanExecuteRecordService {

    @Resource
    private CleanExecuteRecordMapper mapper;

    @Override
    public List<CleanExecuteRecord> listAll() {
        return mapper.selectAll();
    }
}
