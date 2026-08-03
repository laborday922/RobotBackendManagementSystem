package com.ruoyi.qa.Chat.translate;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class ChatTranslationService
{
    private static final Logger log = LoggerFactory.getLogger(ChatTranslationService.class);

    private static final String DETECT_AND_TRANSLATE_PROMPT =
        "你是一个多语言预处理器。请识别用户输入的主要语言，并把原文准确翻译成简体中文。"
            + "只允许输出 JSON，不要输出 markdown、代码块或解释。"
            + "JSON 格式固定为："
            + "{\"language\":\"语言名称\",\"languageCode\":\"IETF 语言代码\",\"translatedText\":\"简体中文翻译\",\"needsAnswerTranslation\":true或false}。"
            + "如果原文已经是简体中文，则 translatedText 返回原文，languageCode 返回 zh-CN，needsAnswerTranslation 返回 false。"
            + "如果原文不是简体中文，则 needsAnswerTranslation 必须返回 true。";

    private final OpenAiTranslationClient translationClient;

    public ChatTranslationService(OpenAiTranslationClient translationClient)
    {
        this.translationClient = translationClient;
    }

    public TranslationPreparation prepareQuery(String query)
    {
        if (!StringUtils.hasText(query))
        {
            return TranslationPreparation.noTranslation(query);
        }
        if (!translationClient.isConfigured())
        {
            return TranslationPreparation.noTranslation(query);
        }

        try
        {
            String content = translationClient.chat(DETECT_AND_TRANSLATE_PROMPT, query);
            JSONObject json = parseJson(content);
            String translatedText = json.getString("translatedText");
            String language = json.getString("language");
            String languageCode = json.getString("languageCode");
            Boolean needsAnswerTranslation = json.getBoolean("needsAnswerTranslation");

            if (!StringUtils.hasText(translatedText))
            {
                translatedText = query;
            }

            String normalizedLanguageCode = normalizeLanguageCode(languageCode);
            boolean answerTranslation = !"zh-CN".equalsIgnoreCase(normalizedLanguageCode)
                || Boolean.TRUE.equals(needsAnswerTranslation);

            return new TranslationPreparation(
                query,
                translatedText,
                StringUtils.hasText(language) ? language.trim() : "Chinese",
                normalizedLanguageCode,
                answerTranslation
            );
        }
        catch (Exception e)
        {
            log.warn("Prepare chat translation failed, fallback to original query: {}", e.getMessage());
            return TranslationPreparation.noTranslation(query);
        }
    }

    public String translateAssistantText(String chineseText, String targetLanguage, String targetLanguageCode)
    {
        if (!StringUtils.hasText(chineseText))
        {
            return chineseText;
        }
        if (!translationClient.isConfigured())
        {
            return chineseText;
        }
        if ("zh-CN".equalsIgnoreCase(normalizeLanguageCode(targetLanguageCode)))
        {
            return chineseText;
        }

        String prompt = "你是一个专业翻译器。请把输入内容从简体中文准确翻译成"
            + (StringUtils.hasText(targetLanguage) ? targetLanguage : targetLanguageCode)
            + "。只返回翻译结果，不要解释、不要加前后缀、不要补充缺失信息、不要输出“注意/Note/原文截断”等提示。"
            + "输入可能是一个片段或不完整句子，也必须直接翻译，不要做任何推断。"
            + "必须把所有可翻译的中文都翻译成目标语言，输出中不应残留中文。"
            + "保留原有换行、Markdown、编号、代码块、URL、变量名、数字。"
            + "对于法规/表单/机构等正式名称：优先翻译为目标语言；如果需要保留原文，请使用“译文（原文）”格式，仅限正式名称使用。";

        try
        {
            return translationClient.chat(prompt, chineseText);
        }
        catch (Exception e)
        {
            log.warn("Translate assistant text failed, fallback to Chinese: {}", e.getMessage());
            return chineseText;
        }
    }

    public boolean isConfigured()
    {
        return translationClient.isConfigured();
    }

    private JSONObject parseJson(String content)
    {
        String normalized = content == null ? "" : content.trim();
        if (normalized.startsWith("```"))
        {
            normalized = normalized.replaceFirst("^```(?:json)?", "").replaceFirst("```$", "").trim();
        }
        return JSON.parseObject(normalized);
    }

    private String normalizeLanguageCode(String languageCode)
    {
        if (!StringUtils.hasText(languageCode))
        {
            return "und";
        }
        return languageCode.trim();
    }

    public static class TranslationPreparation
    {
        private final String originalQuery;
        private final String chineseQuery;
        private final String sourceLanguage;
        private final String sourceLanguageCode;
        private final boolean answerTranslationRequired;

        public TranslationPreparation(String originalQuery, String chineseQuery, String sourceLanguage,
            String sourceLanguageCode, boolean answerTranslationRequired)
        {
            this.originalQuery = originalQuery;
            this.chineseQuery = chineseQuery;
            this.sourceLanguage = sourceLanguage;
            this.sourceLanguageCode = sourceLanguageCode;
            this.answerTranslationRequired = answerTranslationRequired;
        }

        public static TranslationPreparation noTranslation(String query)
        {
            return new TranslationPreparation(query, query, "Chinese", "zh-CN", false);
        }

        public String getOriginalQuery()
        {
            return originalQuery;
        }

        public String getChineseQuery()
        {
            return chineseQuery;
        }

        public String getSourceLanguage()
        {
            return sourceLanguage;
        }

        public String getSourceLanguageCode()
        {
            return sourceLanguageCode;
        }

        public boolean isAnswerTranslationRequired()
        {
            return answerTranslationRequired;
        }
    }
}
