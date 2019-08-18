package com.liuhu.socket.enums;

public enum PersonalStatusEnum {
	//有效
    VALID("1", "有效"),
    //无效
	INVALID("2","无效");
    private String code;
    private String value;

    PersonalStatusEnum(String code, String value) {
        this.code = code;
        this.value = value;
    }

    public String getCode() {
        return code;
    }

    public String getValue() {
        return value;
    }
}
