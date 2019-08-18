package com.liuhu.socket.service;

import com.liuhu.socket.domain.TradeInputDomain;

public interface TradeInfoService {


    int operateTrade(TradeInputDomain input);

    int saleTrade(TradeInputDomain input);
}
