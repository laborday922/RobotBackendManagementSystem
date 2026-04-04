package com.ruoyi.common.enums;

import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;

/**
 * 返回的错误码
 */
public enum ReturnNo {
    /***************************************************
     *    系统级返回码
     **************************************************/

    //状态码 200
    OK(0, "ReturnNo.OK"),
    STATENOTALLOW(7, "ReturnNo.STATENOTALLOW"),
    RESOURCE_FALSIFY(11, "ReturnNo.RESOURCE_FALSIFY"),
    CREATEOBJ_NOTEXIST(18, "ReturnNo.CREATEOBJ_NOTEXIST"),
    SAMEOBJECT(19, "ReturnNo.SAMEOBJECT"),
    DIFFDEPT(26, "ReturnNo.DIFFDEPT"),
    SYSTEMROLE_NOTDELETABLE(21, "ReturnNo.SYSTEMROLE_NOTDELETABLE"),
    CRYPTO_ENCRYPT_FAILED(25, "ReturnNo.CRYPTO_ENCRYPT_FAILED"),
    CRYPTO_DECRYPT_FAILED(22, "ReturnNo.CRYPTO_DECRYPT_FAILED"),
    CATEGORY_IN_USE(23, "ReturnNo.CATEGORY_IN_USE"),
    SOURCE_IN_USE(42, "ReturnNo.SOURCE_IN_USE"),
    ROBOT_STATUS_ABNORMAL(50, "ReturnNo.ROBOT_STATUS_ABNORMAL"),
    DATA_INVALID(30, "ReturnNo.DATA_INVALID"),
    //状态码201
    CREATED(1, "ReturnNo.CREATED"),

    //状态码 404
    RESOURCE_ID_NOTEXIST(4, "ReturnNo.RESOURCE_ID_NOTEXIST"),

    //状态码 500
    INTERNAL_SERVER_ERR(2, "ReturnNo.INTERNAL_SERVER_ERR"),
    APPLICATION_PARAM_ERR(24, "ReturnNo.APPLICATION_PARAM_ERR"),


    //所有需要登录才能访问的API都可能会返回以下错误
    //状态码 400
    FIELD_NOTVALID(3, "ReturnNo.FIELD_NOTVALID"),
    IMG_FORMAT_ERROR(8, "ReturnNo.IMG_FORMAT_ERROR"),
    IMG_SIZE_EXCEED(9, "ReturnNo.IMG_SIZE_EXCEED"),
    PARAMETER_MISSED(10, "ReturnNo.PARAMETER_MISSED"),
    INCONSISTENT_DATA(20, "ReturnNo.INCONSISTENT_DATA"),

    //状态码 401
    AUTH_INVALID_JWT(5, "ReturnNo.AUTH_INVALID_JWT"),
    AUTH_JWT_EXPIRED(6, "ReturnNo.AUTH_JWT_EXPIRED"),
    AUTH_INVALID_ACCOUNT(12, "ReturnNo.AUTH_INVALID_ACCOUNT"),
    AUTH_ID_NOTEXIST(13, "ReturnNo.AUTH_ID_NOTEXIST"),
    AUTH_USER_FORBIDDEN(14, "ReturnNo.AUTH_USER_FORBIDDEN"),
    AUTH_NEED_LOGIN(15, "ReturnNo.AUTH_NEED_LOGIN"),
    USER_NOT_FOUND(35, "ReturnNo.USER_NOT_FOUND"),
    BAD_CREDENTIAL(49,"ReturnNo.BAD_CREDENTIAL"),

    //状态码 403
    AUTH_NO_RIGHT(16, "ReturnNo.AUTH_NO_RIGHT"),
    RESOURCE_ID_OUTSCOPE(17, "ReturnNo.RESOURCE_ID_OUTSCOPE");



    private int errNo;
    private String message;
    private static final Map<Integer, ReturnNo> returnNoMap = new HashMap();
    static {
        for (ReturnNo returnNo : ReturnNo.values()) { // 直接使用 ReturnNo 类型遍历
            returnNoMap.put(returnNo.errNo, returnNo);
        }
    }

    ReturnNo(int code, String message){
        this.errNo = code;
        this.message = message;
    }

    public static ReturnNo getByCode(int code1) {
        ReturnNo[] all=ReturnNo.values();
        for (ReturnNo returnNo :all) {
            if (returnNo.errNo==code1) {
                return returnNo;
            }
        }
        return null;
    }
    public static ReturnNo getReturnNoByCode(int code){
        ReturnNo returnNo = returnNoMap.get(code);
        // 兜底返回系统错误
        Assert.notNull(returnNo, "ReturnNo should not be null");
        return returnNo;
    }
    public int getErrNo() {
        return errNo;
    }

    public String getMessage(){
        return message;
    }


    }
