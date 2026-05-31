package com.ruoyi.data.clean.domain.context;

import com.ruoyi.data.clean.domain.bo.CleanResult;
import com.ruoyi.data.clean.domain.enums.DataSourceType;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
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

    public void addResult(CleanResult result) {
        resultList.add(result);
    }

    // ================== 表字段 ==================

    public void setTableColumns(String tableName, List<String> columns) {
        this.tableColumns.put(tableName, columns);
    }

    public List<String> getTableColumns(String tableName) {
        return this.tableColumns.get(tableName);
    }
}
