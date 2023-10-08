package com.liuhu.socket.dao;


import com.liuhu.socket.entity.TradeCollectInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 交易汇总mapper
 */
public interface TradeCollectInfoMapper {

    int insertSelective(TradeCollectInfo tradeCollectInfo);

    List<TradeCollectInfo> queryDetailInfoByCondition(@Param("tradeCollectInfo") TradeCollectInfo tradeCollectInfo);
}
