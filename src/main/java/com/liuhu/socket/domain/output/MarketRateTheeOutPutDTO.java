package com.liuhu.socket.domain.output;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
public class MarketRateTheeOutPutDTO {

    //花费总额
    private Double amount;

    //收益
    private Double income;

    //当前收益最大金额
    private Double currentAmount;

    //计算天数
    private Integer days;

    private Date startTime;


}
