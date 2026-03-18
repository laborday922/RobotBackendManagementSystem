package com.ruoyi.data.clean.cleanExecuteStrategy;

import com.ruoyi.data.clean.domain.context.DataContext;
import com.ruoyi.data.clean.domain.enums.DataSourceType;
import com.ruoyi.data.clean.domain.enums.FieldIntegrityType;

public class FieldIntegrityStrategy implements CleanStrategy {

    private FieldIntegrityType type;

    public FieldIntegrityStrategy(FieldIntegrityType type) {
        this.type = type;
    }

    @Override
    public void execute(DataContext context) {

        for (DataSourceType source : context.getDataSources()) {

            String table = source.getTableName();

            switch (type) {

                case MARK_EXCEPTION:
                    String markSql =
                            "UPDATE " + table +
                                    " SET is_missing = 1 WHERE important_field IS NULL";
                    context.addSql(markSql);
                    break;

                case FILL_DEFAULT:
                    String fillSql =
                            "UPDATE " + table +
                                    " SET important_field = 'DEFAULT_VALUE' WHERE important_field IS NULL";
                    context.addSql(fillSql);
                    break;
            }
        }
    }
}