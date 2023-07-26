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

    private Integer doubleSize;


    private Integer cycleProfit;


}
