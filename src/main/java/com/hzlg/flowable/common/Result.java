package com.hzlg.flowable.common;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * 统一结果返回
 */
@Data
@NoArgsConstructor
public class Result<T> {


    public Result(String msg, Integer code, Object data) {
        this.message = msg;
        this.code = code;
        this.data = (T) data;
    }

    public Result(String msg, Integer code) {
        this.message = msg;
        this.code = code;
    }

    public Result(Object data) {
        this.data = (T) data;
    }


    /**
     * 消息
     */
    private String message = "成功";
    /**
     * 状态码
     */
    private Integer code = 200;

    /**
     * 请求ID
     */
    private String requestId = UUID.randomUUID().toString();

    /**
     * 数据
     */
    private T data;

    /**
     * 异常信息
     */
    private String ex;

    public void setEx(String ex) {
        this.ex = ex;
    }

    public String getEx() {
        return ex != null && ex.isEmpty() ? null : ex;

    }

    public String getRequestId() {
        return requestId != null && requestId.isEmpty() ? null : requestId;

    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
}
