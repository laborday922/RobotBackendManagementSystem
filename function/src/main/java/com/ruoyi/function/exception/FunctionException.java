package com.ruoyi.function.exception;

/**
 * 功能模块业务异常
 */
public class FunctionException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private String code;
    private Object[] args;

    public FunctionException(String message) {
        super(message);
    }

    public FunctionException(String code, String message) {
        super(message);
        this.code = code;
    }

    public FunctionException(String message, Throwable cause) {
        super(message, cause);
    }

    public FunctionException(String code, String message, Object[] args) {
        super(message);
        this.code = code;
        this.args = args;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }

    // 常用异常构造方法
    public static FunctionException mapNotFound(Long mapId) {
        return new FunctionException("MAP_NOT_FOUND", "地图不存在，mapId=" + mapId);
    }

    public static FunctionException pointNotFound(Long pointId) {
        return new FunctionException("POINT_NOT_FOUND", "点位不存在，pointId=" + pointId);
    }

    public static FunctionException routeNotFound(Long routeId) {
        return new FunctionException("ROUTE_NOT_FOUND", "路线不存在，routeId=" + routeId);
    }

    public static FunctionException fileUploadFailed(String fileName) {
        return new FunctionException("FILE_UPLOAD_FAILED", "文件上传失败：" + fileName);
    }

    public static FunctionException unsupportedFileType(String extension) {
        return new FunctionException("UNSUPPORTED_FILE_TYPE", "不支持的文件类型：" + extension);
    }
}