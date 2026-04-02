package com.ruoyi.taskmgt.utils;

import org.springframework.context.expression.MapAccessor;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ExpressionEvaluator {
    private final SpelExpressionParser parser = new SpelExpressionParser();

    public boolean evaluateBoolean(String expression, Map<String, Object> context) {
        // 创建标准求值上下文，传入根对象（context Map）
        StandardEvaluationContext evalContext = new StandardEvaluationContext(context);

        evalContext.addPropertyAccessor(new MapAccessor());

        return parser.parseExpression(expression).getValue(evalContext, Boolean.class);
    }

    public Object evaluateExpression(String expression, Map<String, Object> context) {
        StandardEvaluationContext evalContext = new StandardEvaluationContext(context);
        evalContext.addPropertyAccessor(new MapAccessor());
        return parser.parseExpression(expression).getValue(evalContext);
    }
}