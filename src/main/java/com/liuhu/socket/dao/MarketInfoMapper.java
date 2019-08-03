package com.liuhu.socket.dao;

import java.util.List;

import com.liuhu.socket.domain.MarketInputDomain;
import com.liuhu.socket.domain.MarketOutputDomain;
import com.liuhu.socket.entity.MarketInfo;
public interface MarketInfoMapper {
	
    int insert(MarketInfo record);

    int insertSelective(MarketInfo record);
     
	List<MarketInfo> getShareInfo(MarketInputDomain input);

    List<MarketOutputDomain> getLastEndList(MarketInputDomain input);
}