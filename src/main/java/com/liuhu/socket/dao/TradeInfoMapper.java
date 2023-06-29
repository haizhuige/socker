package com.liuhu.socket.dao;

import com.liuhu.socket.entity.TradeInfo;

import java.util.List;

public interface TradeInfoMapper {
    int insert(TradeInfo record);

    int insertSelective(TradeInfo record);

    int updateEntity(TradeInfo tradeInfo);

    List<TradeInfo> queryEarlyInfo(TradeInfo tradeInfo);
}