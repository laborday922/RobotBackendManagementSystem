package com.ruoyi.data.clean.domain.context;

import com.ruoyi.data.clean.domain.bo.CleanResult;
import com.ruoyi.data.clean.domain.enums.DataSourceType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataContext {

    // 数据源（可以保留）
    private List<DataSourceType> dataSources;

    // 表字段（你之前做的，也可以保留）
    private Map<String, List<String>> tableColumns = new HashMap<>();

    // ✅ 原始数据（核心）
    private List<Map<String, Object>> rawData;

    // ✅ 清洗结果（核心）
    private List<CleanResult> resultList = new ArrayList<>();

    // ✅ 任务信息（建议加）
    private Long taskId;
    private Long configId;

    public DataContext(List<DataSourceType> dataSources) {
        this.dataSources = dataSources;
    }

    // ================== 原始数据 ==================

    public List<Map<String, Object>> getRawData() {
        return rawData;
    }

    public void setRawData(List<Map<String, Object>> rawData) {
        this.rawData = rawData;
    }

    // ================== 结果数据 ==================

    public void addResult(CleanResult result) {
        resultList.add(result);
    }

    public List<CleanResult> getResultList() {
        return resultList;
    }

    // ================== 表字段 ==================

    public void setTableColumns(String tableName, List<String> columns) {
        this.tableColumns.put(tableName, columns);
    }

    public List<String> getTableColumns(String tableName) {
        return this.tableColumns.get(tableName);
    }

    // ================== 任务信息 ==================

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public Long getConfigId() {
        return configId;
    }

    public void setConfigId(Long configId) {
        this.configId = configId;
    }

    // ================== 数据源 ==================

    public List<DataSourceType> getDataSources() {
        return dataSources;
    }
}