package com.liuhu.socket.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class TradeMethodStrategyConfig {
    private final Map<String, TradeMethodService> paymentStrategies;

    @Autowired
    public  TradeMethodStrategyConfig(Map<String, TradeMethodService> paymentStrategies){
        this.paymentStrategies = paymentStrategies;
    }

    @Bean
    public Map<String, TradeMethodService> getTradeImpl(){
        return paymentStrategies;
    }

}
