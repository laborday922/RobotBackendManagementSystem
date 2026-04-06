package com.ruoyi.mode.exception;

/**
 * 模式模块自定义异常
 */
public class ModeException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private String code;

    public ModeException(String message) {
        super(message);
    }

    public ModeException(String code, String message) {
        super(message);
        this.code = code;
    }

    public ModeException(String message, Throwable cause) {
        super(message, cause);
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
