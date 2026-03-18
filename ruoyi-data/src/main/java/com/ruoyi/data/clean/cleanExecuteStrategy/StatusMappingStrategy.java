package com.ruoyi.data.clean.cleanExecuteStrategy;

import com.ruoyi.data.clean.domain.context.DataContext;
import com.ruoyi.data.clean.domain.enums.DataSourceType;
import com.ruoyi.data.clean.domain.enums.StatusMappingType;

public class StatusMappingStrategy implements CleanStrategy {

    private StatusMappingType type;

    public StatusMappingStrategy(StatusMappingType type) {
        this.type = type;
    }

    @Override
    public void execute(DataContext context) {

        // 🔥 遍历所有数据源
        for (DataSourceType source : context.getDataSources()) {

            String table = source.getTableName();

            switch (type) {

                case KEEP_ORIGINAL:
                    // 不处理
                    break;

                case MAP_TO_PLATFORM_ENUM:
                    String mapSql =
                            "UPDATE " + table +
                                    " SET status_code = CASE status_code " +
                                    "WHEN '0' THEN 'SUCCESS' " +
                                    "WHEN '1' THEN 'FAIL' " +
                                    "ELSE 'UNKNOWN' END";
                    context.addSql(mapSql);
                    break;
            }
        }
    }
}