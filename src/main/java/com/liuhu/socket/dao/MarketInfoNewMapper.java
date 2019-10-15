package com.liuhu.socket.dao;

import com.liuhu.socket.entity.MarketInfoNew;

import java.util.Date;
import java.util.List;

public interface MarketInfoNewMapper {
    int insert(MarketInfoNew record);

    int insertSelective(MarketInfoNew record);

    void insertOrUpdateMarketInfo(List<MarketInfoNew> list);

    Date queryMaxDate(String shareCode);
}