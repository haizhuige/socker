package com.liuhu.socket.enums;

public enum SpecialSockerEnum {
	//A股
   A_SOCKER("000001", "上证代码");


    private String code;
    private String value;

    SpecialSockerEnum(String code, String value) {
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
