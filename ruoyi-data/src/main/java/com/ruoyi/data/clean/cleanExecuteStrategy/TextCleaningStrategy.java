package com.ruoyi.data.clean.cleanExecuteStrategy;

import com.ruoyi.data.clean.domain.context.DataContext;
import com.ruoyi.data.clean.domain.enums.DataSourceType;
import com.ruoyi.data.clean.domain.enums.TextCleaningType;

public class TextCleaningStrategy implements CleanStrategy {

    private TextCleaningType type;

    public TextCleaningStrategy(TextCleaningType type) {
        this.type = type;
    }

    @Override
    public void execute(DataContext context) {

        // 🔥 遍历所有数据源
        for (DataSourceType source : context.getDataSources()) {

            String table = source.getTableName();

            switch (type) {

                case REMOVE_HTML:
                    String htmlSql =
                            "UPDATE " + table +
                                    " SET text_field = REGEXP_REPLACE(text_field, '<[^>]*>', '')";
                    context.addSql(htmlSql);
                    break;

                case REMOVE_SPECIAL_CHAR:
                    String specialSql =
                            "UPDATE " + table +
                                    " SET text_field = REGEXP_REPLACE(text_field, '[^a-zA-Z0-9\\u4e00-\\u9fa5]', '')";
                    context.addSql(specialSql);
                    break;

                case KEEP_ORIGINAL:
                    // 不生成SQL
                    break;
            }
        }
    }
}