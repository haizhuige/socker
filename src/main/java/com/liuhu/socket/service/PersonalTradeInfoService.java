package com.liuhu.socket.service;

import com.liuhu.socket.domain.input.*;
import com.liuhu.socket.domain.output.HandleSockerTargetPriceOutDomain;
import com.liuhu.socket.domain.output.MarketOutputDomain;
import com.liuhu.socket.domain.output.QueryFixSerialDownOutDTO;
import com.liuhu.socket.entity.TradeDateInfo;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface PersonalTradeInfoService {


    int operateTrade(TradeInputDomain input);


    List<HandleSockerTargetPriceOutDomain> getHandlePrice(HandleSockerInputDomain handleSockerInputDomain);
}
