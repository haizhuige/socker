package com.liuhu.socket.domain.input;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class GetRateThreeIncomeInputDTO implements Serializable {

    private String type;

    private String methodType;

    private String queryYear;

    private Double minMarketValue;

    private Double maxMarketValue;

    private Double minTurnRate;

    private Integer doubleSize=4;


    private Integer cycleProfit;

    private Double  fee=0.1;


    private Double profit=1.2;

    private Double fundProfit = 0.45;

    private Double doubleProfit=1.5;

    //当前运行负利率
    private Integer runRatio=-10;

    private String queryStartTime;

    private String queryEndTime;

    private String hushenStatus;
    //查询下跌区间时间
    private Integer recentDay=10;

    private Integer minDownRate= 2;

    private Integer minDownDay = 6;

    private Integer rowNum;

    private Integer queryPeriod=10;

    private Double dealAmount= 5000000.0;

    private String shareCode;

}
