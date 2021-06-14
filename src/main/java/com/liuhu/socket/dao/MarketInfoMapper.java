package com.liuhu.socket.dao;

import java.util.Date;
import java.util.List;

import com.liuhu.socket.domain.input.MarketInputDomain;
import com.liuhu.socket.dto.SockerExcelEntity;
import com.liuhu.socket.entity.MarketInfo;
public interface MarketInfoMapper {
	
    int insert(MarketInfo record);

    int insertSelective(MarketInfo record);
     
	List<MarketInfo> getShareInfo(MarketInputDomain input);

    List<MarketInfo> getLastEndList(MarketInputDomain input);

    void insertOrUpdateMarketInfo(List<SockerExcelEntity> list);

    Date queryMaxDate(String shareCode);
}