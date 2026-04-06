package com.liuhu.socket.service;

import com.liuhu.socket.domain.input.GetRateThreeIncomeInputDTO;
import com.liuhu.socket.domain.input.QueryRecentSerialRedConditionDTO;
import com.liuhu.socket.domain.input.QueryStockReturnDTO;
import com.liuhu.socket.domain.output.MarketRateTheeOutPutDTO;
import com.liuhu.socket.domain.output.QueryRecentSerialRedOutPutDTO;

import java.util.List;

public interface TradeMethodService {

    List<QueryRecentSerialRedOutPutDTO> getRecentFinalRatioStrategy(QueryRecentSerialRedConditionDTO marketInput2Domain);

    List<QueryRecentSerialRedOutPutDTO> queryVRatioFromDownStartPoint(QueryRecentSerialRedConditionDTO input2Domain) throws Exception;

    List<QueryRecentSerialRedOutPutDTO> queryThreeDownRatioByDate(QueryRecentSerialRedConditionDTO input2Domain)throws Exception;

    /**
     * 计算股票收益率并存储到 serial_temp 表（优化版）
     * @param inputDTO 查询参数
     * @return 执行结果列表
     */
    List<QueryRecentSerialRedOutPutDTO> calculateStockReturn(QueryStockReturnDTO inputDTO) throws Exception;

    MarketRateTheeOutPutDTO getRateThreeIncome(GetRateThreeIncomeInputDTO getRateThreeIncomeInputDTO);

    List<QueryRecentSerialRedOutPutDTO> queryThreeUpThenAndPreDownRegular(QueryRecentSerialRedConditionDTO input);

    List<QueryRecentSerialRedOutPutDTO> queryFirstBuyMoreThenMarketRatio(GetRateThreeIncomeInputDTO getRateThreeIncomeInputDTO);
}
