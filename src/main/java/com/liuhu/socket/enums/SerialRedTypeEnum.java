package com.liuhu.socket.enums;

/**
 * 连续下跌之后 又连续增长之后的收益率类型
 */
public enum SerialRedTypeEnum {
	//单日
    SINGLE("1", "单日"),
    //区间日
    PERIOD("2","区间日");

    private String code;
    private String value;

    SerialRedTypeEnum(String code, String value) {
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
