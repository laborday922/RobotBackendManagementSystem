package com.ruoyi.data.clean.cleanExecuteStrategy;

import com.ruoyi.data.clean.domain.context.DataContext;
import com.ruoyi.data.clean.domain.enums.DataSourceType;
import com.ruoyi.data.clean.domain.enums.OutlierStrategyType;

public class OutlierStrategy implements CleanStrategy {

    private OutlierStrategyType type;

    public OutlierStrategy(OutlierStrategyType type) {
        this.type = type;
    }

    @Override
    public void execute(DataContext context) {

        // 🔥 遍历所有数据源
        for (DataSourceType source : context.getDataSources()) {

            String table = source.getTableName();

            switch (type) {

                case MARK_EXCEPTION:
                    context.addSql(
                            "UPDATE " + table +
                                    " SET is_abnormal = 1 " +
                                    "WHERE value < min OR value > max"
                    );
                    break;

                case FILTER_DIRECTLY:
                    context.addSql(
                            "DELETE FROM " + table +
                                    " WHERE value < min OR value > max"
                    );
                    break;
            }
        }
    }
}