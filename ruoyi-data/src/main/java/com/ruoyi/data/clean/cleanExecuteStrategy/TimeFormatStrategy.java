package com.ruoyi.data.clean.cleanExecuteStrategy;

import com.ruoyi.data.clean.domain.context.DataContext;
import com.ruoyi.data.clean.domain.enums.DataSourceType;
import com.ruoyi.data.clean.domain.enums.TimeFormatType;

public class TimeFormatStrategy implements CleanStrategy {

    private TimeFormatType type;

    public TimeFormatStrategy(TimeFormatType type) {
        this.type = type;
    }

    @Override
    public void execute(DataContext context) {

        // 🔥 遍历所有数据源
        for (DataSourceType source : context.getDataSources()) {

            String table = source.getTableName();

            switch (type) {

                case ISO_8601:
                    String isoSql =
                            "UPDATE " + table +
                                    " SET time_field = DATE_FORMAT(time_field, '%Y-%m-%dT%H:%i:%s')";
                    context.addSql(isoSql);
                    break;

                case TIMESTAMP_MILLI:
                    String milliSql =
                            "UPDATE " + table +
                                    " SET time_field = UNIX_TIMESTAMP(time_field) * 1000";
                    context.addSql(milliSql);
                    break;

                case TIMESTAMP_SECOND:
                    String secondSql =
                            "UPDATE " + table +
                                    " SET time_field = UNIX_TIMESTAMP(time_field)";
                    context.addSql(secondSql);
                    break;
            }
        }
    }
}