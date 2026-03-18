package com.ruoyi.data.clean.cleanExecuteStrategy;

import com.ruoyi.data.clean.domain.context.DataContext;
import com.ruoyi.data.clean.domain.enums.DataSourceType;
import com.ruoyi.data.clean.domain.enums.DuplicateHandlingType;

import java.util.List;
import java.util.stream.Collectors;

public class DuplicateHandlingStrategy implements CleanStrategy {

    private DuplicateHandlingType type;
    private static final String PRIMARY_KEY = "id"; // 假设主键名为 id，如果可能变化可改为动态获取

    public DuplicateHandlingStrategy(DuplicateHandlingType type) {
        this.type = type;
        //测试
        System.out.println("DuplicateHandlingStrategy created with type: " + type);
    }

    @Override
    public void execute(DataContext context) {
        //测试
        System.out.println("DuplicateHandlingStrategy.execute() called");

        for (DataSourceType source : context.getDataSources()) {
            String table = source.getTableName();

            // 获取所有字段，并排除主键
            List<String> allColumns = context.getTableColumns(table);
            if (allColumns == null || allColumns.isEmpty()) {
                throw new RuntimeException("未获取到表字段信息");
            }

            // 过滤掉主键列
            List<String> columns = allColumns.stream()
                    .filter(col -> !PRIMARY_KEY.equalsIgnoreCase(col))
                    .collect(Collectors.toList());

            if (columns.isEmpty()) {
                throw new RuntimeException("表中没有可比较的非主键字段，无法进行重复处理");
            }

            // 构造连接条件
            String joinCondition = columns.stream()
                    .map(col -> "t1." + col + " = t2." + col)
                    .collect(Collectors.joining(" AND "));

            // MySQL 多表删除语法
            String sql = "DELETE t1 FROM " + table + " t1 " +
                    "INNER JOIN " + table + " t2 " +
                    "ON " + joinCondition + " " +
                    "WHERE t1.id > t2.id";
            //测试
            System.out.println("Generated SQL: " + sql);

            context.addSql(sql);
        }
    }
}