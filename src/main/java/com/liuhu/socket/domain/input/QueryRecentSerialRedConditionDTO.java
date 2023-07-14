package com.liuhu.socket.domain.input;


import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Setter
@Getter
public class QueryRecentSerialRedConditionDTO  implements Serializable {

    //查询类型   1：代表当日  2：区间查询
    private String type ="1";
    //查询区间 查询多少天的增长值
    private Integer period =20;
    //查询区间 开始时间
    private String endTime;
    //筛选条件最小市值
    private Long minMarketValue =80000000L;
    //筛选连续增长期间最小值增长总收益率
    private Integer minRate =3;
    //筛选连续增长期间最小值增长天数
    private Integer minUpDay =3;
    //筛选连续下跌期间设置的最小下跌天数
    private Integer minDownDay =7;
    //当type为当日时，筛选待选代码的日期
    private String selectStartTime;
    //连续增长区间天数
    private Integer  periodUpDay = 5;
    //连续下跌区间天数
    private Integer  periodDownDay = 10;
    //最近计算增长率的天数
    private Integer recentRateDay = 5;
    //最小换手率
    private Integer minTurnOverRate = 2;

    private List<String> shareCodeList;

    private String selectEndTime;

    private Integer rateOrAmountDay=5;



}
