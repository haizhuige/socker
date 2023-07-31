package com.liuhu.socket.domain.output;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.Map;

@Setter
@Getter
public class MarketRateTheeOutPutDTO {

    //收益金额
    private Double amount;

    //收益
    private Double income;

    //当前收益最大金额
    private Double currentAmount;

    //从购买当日到结束按照finalRatio的总收益率
    private Double handleFinalRatio;

    //计算天数
    private Integer days;

    //最大仓位数
    private Integer maxCount;

    private Date startTime;

    private Map<Integer,Double> allIncomeMap;

    private  Double periodRatio;


}
