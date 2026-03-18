package com.ruoyi.data.clean.domain.context;

import com.ruoyi.data.clean.domain.enums.DataSourceType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataContext {

    private List<DataSourceType> dataSources;

    private List<String> sqlList = new ArrayList<>();

    private Map<String, List<String>> tableColumns = new HashMap<>();

    public DataContext(List<DataSourceType> dataSources) {
        this.dataSources = dataSources;
    }

    public void addSql(String sql) {
        sqlList.add(sql);
    }

    public List<DataSourceType> getDataSources() {
        return dataSources;
    }

    public List<String> getSqlList() {
        return sqlList;
    }

    public void setTableColumns(String tableName, List<String> columns) {
        this.tableColumns.put(tableName, columns);
    }

    public List<String> getTableColumns(String tableName) {
        return this.tableColumns.get(tableName);
    }
}