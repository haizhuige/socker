package com.liuhu.socket.service;

import com.liuhu.socket.domain.input.MarketDetailInputDomain;
import com.liuhu.socket.domain.input.QueryRecentSerialRedConditionDTO;
import com.liuhu.socket.domain.input.TradeInputDomain;
import com.liuhu.socket.domain.output.MarketOutputDomain;
import com.liuhu.socket.domain.output.MarketRateTheeOutPutDTO;
import com.liuhu.socket.entity.TradeDateInfo;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface TradeInfoService {


    int operateTrade(TradeInputDomain input);

    int saleTrade(TradeInputDomain input);

    Map ownerLongIncome(MarketDetailInputDomain input);


    TradeDateInfo queryMaxDate();

    Date getWantDate(Integer recentDay,Date date,String type);

    List<Date> queryPeriodDateList( String endTime, Integer period,String flag);

    MarketRateTheeOutPutDTO getRateThreeIncome(Integer type);

    List<MarketOutputDomain> getPrePurchaseSocker(QueryRecentSerialRedConditionDTO input);

    Map<String,Object> getPreFiveAndSubFive(QueryRecentSerialRedConditionDTO input);


}
