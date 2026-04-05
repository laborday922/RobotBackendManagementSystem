package com.ruoyi.common.exception.task;

import com.ruoyi.common.enums.ReturnNo;
import com.ruoyi.common.exception.base.BaseException;

/**
 * 机器人任务管理异常类
 */
public class TaskmgtException extends BaseException {
    private static final long serialVersionUID = 1L;

    private ReturnNo returnNo; // 保存枚举，用于获取错误码和消息 key

    /**
     * 构造方法
     *
     * @param returnNo 返回码枚举
     * @param args     国际化消息参数
     * @param message 返回消息
     */
    public TaskmgtException(ReturnNo returnNo, Object[] args, String message) {
        super("taskmgt", returnNo.getMessage(), args, message);
        this.returnNo = returnNo;
    }

    /**
     * 获取返回码枚举
     */
    public ReturnNo getReturnNo() {
        return returnNo;
    }

    /**
     * 获取错误码（数字）
     */
    public int getErrNo() {
        return returnNo.getErrNo();
    }
}