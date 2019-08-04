package com.liuhu.socket.enums;

public enum SockerStatusEnum {
	//A股
   GROUNDING ("1", "上架"),
   TAKE_OFF_SHELVES("0","下架");

    private String code;
    private String value;

    SockerStatusEnum(String code, String value) {
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
