package com.liuhu.socket.dto;

import com.liuhu.socket.entity.MarketInfoNew;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Setter
@Getter
public class QueryRecentSerialRedConditionDO {

    //筛选条件最小市值
    private Long minMarketValue =80000000L;
    //筛选连续增长期间最小值增长总收益率
    private Integer minRate =3;
    //筛选连续增长期间最小值增长天数
    private Integer minUpDay =3;
    //筛选连续下跌期间设置的最小下跌天数
    private Integer minDownDay =7;
    //连续增长区间天数
    private Integer  periodUpDay = 5;
    //连续下跌区间天数
    private Integer  periodDownDay = 10;
    //最近计算增长率的天数
    private Integer recentRateDay = 5;
    //最小换手率
    private Integer minTurnOverRate = 2;

    //筛选待选代码的日期
    private Date selectStartTime;

    //筛选待选代码的日期
    private Date selectEndTime;
    //连续下跌的开始日期
    private Date downStartTime;
    //连续下跌的结束日期
    private Date downEndTime;
    //连续上涨的开始日期
    private Date upStartTime;
    //连续上涨的结束日期
    private Date upEndTime;
    // 1:达到最小红数量，第二日作为购买日起始日的标识
    private String purchaseFlag;
    //满足红了区间的shareCode
    private List<MarketInfoNew> marketList;

    private List<String> shareCodeList;



}
