package com.liuhu.socket.service;

import com.liuhu.socket.domain.input.GetRateThreeIncomeInputDTO;
import com.liuhu.socket.domain.input.QueryRecentSerialRedConditionDTO;
import com.liuhu.socket.domain.output.MarketRateTheeOutPutDTO;
import com.liuhu.socket.domain.output.QueryRecentSerialRedOutPutDTO;

import java.util.List;

public interface TradeMethodService {

    List<QueryRecentSerialRedOutPutDTO> getRecentFinalRatioStrategy(QueryRecentSerialRedConditionDTO marketInput2Domain);

    List<QueryRecentSerialRedOutPutDTO> queryVRatioFromDownStartPoint(QueryRecentSerialRedConditionDTO input2Domain) throws Exception;

    List<QueryRecentSerialRedOutPutDTO> queryThreeDownRatioByDate(QueryRecentSerialRedConditionDTO input2Domain)throws Exception;

    MarketRateTheeOutPutDTO getRateThreeIncome(GetRateThreeIncomeInputDTO getRateThreeIncomeInputDTO);

    List<QueryRecentSerialRedOutPutDTO> queryThreeUpThenAndPreDownRegular(QueryRecentSerialRedConditionDTO input);

    List<QueryRecentSerialRedOutPutDTO> queryFirstBuyMoreThenMarketRatio(GetRateThreeIncomeInputDTO getRateThreeIncomeInputDTO);
}
