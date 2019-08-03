package com.liuhu.socket.dao;

import com.liuhu.socket.entity.TradeInfo;

public interface TradeInfoMapper {
    int insert(TradeInfo record);

    int insertSelective(TradeInfo record);
}