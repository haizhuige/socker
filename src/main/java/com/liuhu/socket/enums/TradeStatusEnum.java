package com.liuhu.socket.enums;

public enum TradeStatusEnum {
	//正常购买
    NORMAL_BUY(0, "正常购买"),
    //开盘卖尾盘买
	OPEN_SALE_TAIL_BUY(1,"开盘卖尾盘买"),

    BUY(1, "购买"),

    SALE(2, "赎回");
    private Integer code;
    private String value;

    TradeStatusEnum(Integer code, String value) {
        this.code = code;
        this.value = value;
    }

    public Integer getCode() {
        return code;
    }

    public String getValue() {
        return value;
    }
}
