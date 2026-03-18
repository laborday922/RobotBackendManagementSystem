package com.ruoyi.data.clean.service;

import com.ruoyi.data.clean.domain.CleanExecuteHistory;

import java.util.List;

public interface CleanExecuteHistoryService {

    Long createRecord(CleanExecuteHistory history);

    CleanExecuteHistory getById(Long id);

    List<CleanExecuteHistory> listAll();

    void updateStatus(Long id, Integer status);

    void delete(Long id);
}