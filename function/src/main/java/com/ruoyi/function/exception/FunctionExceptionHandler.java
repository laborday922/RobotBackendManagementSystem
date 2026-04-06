package com.ruoyi.function.exception;

import com.ruoyi.common.core.domain.AjaxResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 功能模块全局异常处理器
 */
@RestControllerAdvice(basePackages = "com.ruoyi.function.controller")
public class FunctionExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(FunctionExceptionHandler.class);

    /**
     * 业务异常处理
     */
    @ExceptionHandler(FunctionException.class)
    public AjaxResult handleFunctionException(FunctionException e) {
        log.error("业务异常：{}", e.getMessage());
        return AjaxResult.error(e.getMessage());
    }

    /**
     * 参数校验异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public AjaxResult handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("参数异常：{}", e.getMessage());
        return AjaxResult.error("参数错误：" + e.getMessage());
    }

    /**
     * 运行时异常处理
     */
    @ExceptionHandler(RuntimeException.class)
    public AjaxResult handleRuntimeException(RuntimeException e) {
        log.error("运行时异常：", e);
        return AjaxResult.error("系统内部错误");
    }

    /**
     * 通用异常处理
     */
    @ExceptionHandler(Exception.class)
    public AjaxResult handleException(Exception e) {
        log.error("系统异常：", e);
        return AjaxResult.error("系统繁忙，请稍后再试");
    }
}