package com.liuhu.socket.service;

import com.liuhu.socket.domain.input.*;
import com.liuhu.socket.domain.output.MarketOutputDomain;
import com.liuhu.socket.domain.output.MarketRateTheeOutPutDTO;
import com.liuhu.socket.domain.output.QueryFixSerialDownOutDTO;
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



    List<MarketOutputDomain> getPrePurchaseSocker(QueryRecentSerialRedConditionDTO input);

    Map<String,Object> getPreFiveAndSubFive(QueryRecentSerialRedConditionDTO input);

    Map<Date,List<String>> getFixSerialDown(QueryProfitByComProgram queryProfitByComProgram);

    MarketOutputDomain getProfitFromSerialDown(QueryProfitByComProgram queryProfitByComProgram);

    List<QueryFixSerialDownOutDTO> getPreSelectionSerialDownDTOList(QueryFixSerialDownInDTO conditionDTO);


    List<QueryFixSerialDownOutDTO> preSelectionGetSerialDownOfRiver(QueryFixSerialDownInDTO conditionDTO);


}
