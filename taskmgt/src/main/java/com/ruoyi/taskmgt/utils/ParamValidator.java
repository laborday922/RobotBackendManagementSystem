package com.ruoyi.taskmgt.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.app.domain.TAppParam;
import org.springframework.util.StringUtils;

public class ParamValidator {

    public static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 校验参数值是否符合应用参数中定义的 validation_rule
     * @param param 应用参数定义（包含 param_type, validation_rule 字符串）
     * @param value 待校验的值（已从用户覆盖或表单中提取）
     * @throws ValidationException 如果校验失败
     */
    public static void validateParamValue(TAppParam param, Object value) {
        if (value == null) {
            // 根据业务需求处理 null 值，如果是必填参数则抛出异常
            return;
        }
        String ruleStr = param.getValidationRule();   // 获取 JSON 字符串
        if (!StringUtils.hasText(ruleStr)) {
            return; // 无校验规则
        }
        JsonNode rule;
        try {
            rule = objectMapper.readTree(ruleStr);
        } catch (Exception e) {
            // 如果规则解析失败，可选择记录日志并忽略校验，或抛出异常
            return;
        }
        if (rule == null || rule.isNull()) {
            return;
        }

        String type = rule.path("type").asText();
        switch (type) {
            case "range":
                validateRange(param, value, rule);
                break;
            case "enum":
                validateEnum(param, value, rule);
                break;
            case "regex":
                validateRegex(param, value, rule);
                break;
            default:
                // 未知规则，忽略
                break;
        }
    }

    private static void validateRange(TAppParam param, Object value, JsonNode rule) {
        // 只支持数值类型
        if (!(value instanceof Number)) {
            throw new ValidationException(param.getParamName() + " 必须为数字类型");
        }
        double num = ((Number) value).doubleValue();
        double min = rule.path("min").asDouble(Double.NEGATIVE_INFINITY);
        double max = rule.path("max").asDouble(Double.POSITIVE_INFINITY);
        if (num < min || num > max) {
            throw new ValidationException(param.getParamName() + " 必须在 " + min + " 到 " + max + " 之间");
        }
    }

    private static void validateEnum(TAppParam param, Object value, JsonNode rule) {
        JsonNode valuesNode = rule.path("values");
        if (!valuesNode.isArray()) {
            return;
        }
        String strValue = value.toString();
        for (JsonNode v : valuesNode) {
            if (v.asText().equals(strValue)) {
                return;
            }
        }
        throw new ValidationException(param.getParamName() + " 只能是 " + valuesNode);
    }

    private static void validateRegex(TAppParam param, Object value, JsonNode rule) {
        if (!(value instanceof String)) {
            throw new ValidationException(param.getParamName() + " 必须为字符串");
        }
        String pattern = rule.path("pattern").asText();
        if (!((String) value).matches(pattern)) {
            throw new ValidationException(param.getParamName() + " 格式不正确");
        }
    }

    // 自定义异常类
    public static class ValidationException extends RuntimeException {
        public ValidationException(String message) {
            super(message);
        }
    }
}