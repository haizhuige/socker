package com.liuhu.socket.common;

import java.io.Serializable;

public class ResponseResult<T> implements Serializable {

	private static final long serialVersionUID = 2131351656081166487L;
	
	public static ResponseResult done = new ResponseResult(true);

    public static <T> ResponseResult done(T data) {
        ResponseResult result = new ResponseResult(true);
        result.setData(data);
        return result;
    }
    private Boolean success;
    private String msg;
    private T data;

    public ResponseResult(Boolean success) {
        this.success = success;
    }

    public static ResponseResult failed(String msg) {
        ResponseResult result = new ResponseResult(false);
        result.setMsg(msg);
        return result;
    }

    public ResponseResult success(String msg, T data) {
        ResponseResult result = new ResponseResult(true);
        result.msg = msg;
        result.data = data;
        return result;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(final String msg) {
        this.msg = msg;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(final Boolean success) {
        this.success = success;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
