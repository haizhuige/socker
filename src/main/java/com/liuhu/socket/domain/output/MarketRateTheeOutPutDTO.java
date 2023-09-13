package com.liuhu.socket.domain.output;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Setter
@Getter
public class MarketRateTheeOutPutDTO {

    //收益金额
    private Double amount=0.0;

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

    private Integer maxDoubleSize;

    private String maxShareCode;

    private List<RecentDownOrderOutPutDTO> orderOutPutDTOList;

    private Date endTime;

    private Boolean isFinish=false;


    private Map<Date,Integer> doubleSizeMap = new HashMap<>();

    private Double allProfit = 0.0;

    private  Double tFinalRatio = 0.0;

    private  Double methodRunRatio = 0.0;

    private Double unitProfit=0.0;


    private Integer k = 0;



}
