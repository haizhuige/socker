package com.liuhu.socket.service;

import com.liuhu.socket.domain.input.MarketInput2Domain;
import com.liuhu.socket.domain.input.QueryRecentSerialRedConditionDTO;
import com.liuhu.socket.domain.output.QueryRecentSerialRedOutPutDTO;

import java.util.List;

public interface TradeMethodService {

    List<QueryRecentSerialRedOutPutDTO> getRecentFinalRatioStrategy(QueryRecentSerialRedConditionDTO marketInput2Domain);
}
