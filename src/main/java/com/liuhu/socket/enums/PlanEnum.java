package com.liuhu.socket.enums;

/**
 * @Author liuhu-jk
 * @Date 2023/8/24 17:21
 * @Description
 **/
public enum PlanEnum {
    FIX_SERIAL_DOWN("1", "连续绿达到固定数额"),
    SERIAL_TWO_RIVER_DOWN("2", "连续两天水下"),
    PRE_DOWN_AND_FIRST_UP("3","前期跌区间，首次上涨"),
    BLUE_SERIAL_TWO_RIVER_DOWN("4","蓝筹两连水下"),
    MIX_SERIAL_TWO_RIVER_DOWN_NEX_LOW_DOWN("5","中等市值连续两连水下,次日低开水下");

    private String code;
    private String desc;

    PlanEnum(String code, String desc) {
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
