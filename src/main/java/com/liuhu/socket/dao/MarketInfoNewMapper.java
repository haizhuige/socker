package com.liuhu.socket.dao;

import com.liuhu.socket.domain.input.MarketDetailInputDomain;
import com.liuhu.socket.domain.input.MarketInput2Domain;
import com.liuhu.socket.entity.MarketInfoNew;
import com.liuhu.socket.entity.ShareInfo;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface MarketInfoNewMapper {
    int insert(MarketInfoNew record);

    int insertSelective(MarketInfoNew record);

    void insertOrUpdateMarketInfo(List<MarketInfoNew> list);

    Date queryMaxDate(@Param("shareCode") String shareCode);

    List<Map> queryPeriodRateByShareCode(@Param("domain") MarketInput2Domain marketInput2Domain);

    List<MarketInfoNew> queryMarketInfoByParam(MarketDetailInputDomain input);

    List<Date> queryDistinctDate();

    List<String> queryMaxAmount();
}