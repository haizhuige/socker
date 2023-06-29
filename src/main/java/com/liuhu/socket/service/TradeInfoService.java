package com.liuhu.socket.service;

import com.liuhu.socket.domain.input.MarketDetailInputDomain;
import com.liuhu.socket.domain.input.TradeInputDomain;

import java.util.Map;

public interface TradeInfoService {


    int operateTrade(TradeInputDomain input);

    int saleTrade(TradeInputDomain input);

    Map ownerLongIncome(MarketDetailInputDomain input);
}
