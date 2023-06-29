package com.liuhu.socket.common;

import com.liuhu.socket.enums.RespFlagEnum;

import java.io.Serializable;

public class ResponseResult<T> implements Serializable {

	private static final long serialVersionUID = 2131351656081166487L;
    private String flag;
    private String code;
    private String msg;
    private T data;
    private Boolean success;
	public static ResponseResult done = new ResponseResult(true);

    public static <T> ResponseResult done(T data) {
        ResponseResult result = new ResponseResult(true);
        result.setData(data);
        return result;
    }


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
    public ResponseResult fail(ResponseService responseService) {
        this.code = responseService.getResponseCode();
        this.msg = responseService.getResponseMessage();
        return this.fail();
    }

    public ResponseResult fail(ResponseService responseService, T data) {
        this.code = responseService.getResponseCode();
        this.msg = responseService.getResponseMessage();
        this.data = data;
        return this.fail();
    }
    public ResponseResult fail() {
        this.flag = RespFlagEnum.FAIL.getCode();
        return this;
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
