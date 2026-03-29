package com.ruoyi.taskmgt.utils;

import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ExpressionEvaluator {

    private final ExpressionParser parser = new SpelExpressionParser();

    /**
     * 安全地计算表达式，返回布尔结果
     * @param expression 表达式字符串，如 "form_data.weight <= app_param.max_load_weight"
     * @param context 上下文变量，如 {"form_data": {...}, "app_param": {...}}
     * @return 表达式计算结果（布尔值）
     * @throws IllegalArgumentException 如果表达式语法错误或计算异常
     */
    public boolean evaluateBoolean(String expression, Map<String, Object> context) {
        try {
            StandardEvaluationContext evaluationContext = new StandardEvaluationContext();

            for (Map.Entry<String, Object> entry : context.entrySet()) {
                evaluationContext.setVariable(entry.getKey(), entry.getValue());
            }

            evaluationContext.setRootObject(context);
            Expression expr = parser.parseExpression(expression);
            return expr.getValue(evaluationContext, Boolean.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("表达式求值失败: " + expression, e);
        }
    }
    public Object evaluateExpression(String expression, Map<String, Object> context) {
        try {
            StandardEvaluationContext evaluationContext = new StandardEvaluationContext();
            evaluationContext.setRootObject(context);
            Expression expr = parser.parseExpression(expression);
            return expr.getValue(evaluationContext);
        } catch (Exception e) {
            throw new IllegalArgumentException("表达式计算失败: " + expression,e);
        }
    }

}