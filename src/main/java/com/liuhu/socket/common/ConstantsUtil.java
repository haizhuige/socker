package com.liuhu.socket.common;

public class ConstantsUtil {

    //操作买卖依据利率升降周期天数(13天 的利率 0.4%,26天的利率为0.8%)
    public static final Integer stepCycle = 13;
    //当天购买卖出的预期收益率
    public static final Double initSaleRatio = 0.4;
    //加减仓位的一个单位利率
    public static final Double unitHandleRatio = 4.0;
    //尾盘操作最早时间点
    public static final Integer earlyHandleTime = 14;
}
