package com.hzlg.flowable.common;

public class ResultCode {

    /**
     * 成功
     */
    public static final int SUCCESS = 200;

    /**
     * token失效
     */
    public static final int NO_LOGIN = 401;

    /**
     * 没有权限访问
     */
    public static final Integer NO_PRIVILEGE = 403;

    /**
     * 参数异常
     */
    public static final int PARAM_EXCEPTION = 400;

    /**
     * 任务不存在
     */
    public static final int TASK_NOT_EXIST = 600;

    /**
     * 表单不存在
     */
    public static final int FORM_NOT_EXIST = 601;
}
