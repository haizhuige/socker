package com.liuhu.socket.dao;


import com.liuhu.socket.entity.TradeInfoDetail;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TradeDetailInfoMapper {

    int insertSelective(TradeInfoDetail tradeInfoDetail);

    List<TradeInfoDetail> queryDetailInfoByCondition(@Param("condition") TradeInfoDetail tradeInfoDetail);
}
