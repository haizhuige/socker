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

    private Double  fee=0.2;


    private Double profit=1.2;

    private Double doubleProfit=1.5;

    //当前运行负利率
    private Integer runRatio=-10;

}
