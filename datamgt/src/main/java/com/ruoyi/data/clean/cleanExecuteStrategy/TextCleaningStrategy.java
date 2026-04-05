package com.ruoyi.data.clean.cleanExecuteStrategy;

import com.ruoyi.data.clean.domain.context.DataContext;
import com.ruoyi.data.clean.domain.enums.TextCleaningType;

public class TextCleaningStrategy implements CleanStrategy {

    private final TextCleaningType type;

    public TextCleaningStrategy(TextCleaningType type) {
        this.type = type;
    }

    @Override
    public String process(Object input, DataContext context) {

        String text = (String) input;

        if (text == null) return null;

        switch (type) {

            case REMOVE_HTML:
                return text.replaceAll("<[^>]*>", "");

            case REMOVE_SPECIAL_CHAR:
                return text.replaceAll("[^\\u4e00-\\u9fa5a-zA-Z0-9 ]", "");

            case KEEP_ORIGINAL:
            default:
                return text;
        }
    }
}