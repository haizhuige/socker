package com.liuhu.socket.enums;

/**
 * @Author liuhu-jk
 * @Date 2019/8/16 17:21
 * @Description
 **/
public enum RespFlagEnum {
    SUCCESS("S", "成功"),
    FAIL("F", "失败");

    private String code;
    private String desc;

    RespFlagEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
