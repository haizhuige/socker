package com.liuhu.socket.dao;

import java.util.Date;
import java.util.List;

import com.liuhu.socket.domain.input.MarketInputDomain;
import com.liuhu.socket.domain.input.QueryRecentSerialRedConditionDTO;
import com.liuhu.socket.domain.output.MarketOutputDomain;
import com.liuhu.socket.domain.output.QueryRecentSerialRedOutPutDTO;
import com.liuhu.socket.dto.QueryRecentSerialRedConditionDO;
import com.liuhu.socket.dto.SockerExcelEntity;
import com.liuhu.socket.entity.MarketInfo;
import com.liuhu.socket.entity.MarketInfoNew;
import org.apache.ibatis.annotations.Param;

public interface MarketInfoMapper {
	
    int insert(MarketInfo record);

    int insertSelective(MarketInfo record);
     
	List<MarketInfo> getShareInfo(MarketInputDomain input);

    List<MarketInfo> getLastEndList(MarketInputDomain input);

    void insertOrUpdateMarketInfo(List<SockerExcelEntity> list);

    Date queryMaxDate(String shareCode);

    List<QueryRecentSerialRedOutPutDTO> queryRecentSerialRed(QueryRecentSerialRedConditionDO queryRecentSerialRedConditionDO);

    List<MarketInfoNew> queryRecentSerialRedExact(QueryRecentSerialRedConditionDO queryRecentSerialRedConditionDO);

    List<QueryRecentSerialRedOutPutDTO> queryRecentSerialRedWithHavingShareCode(@Param("condition") QueryRecentSerialRedConditionDO queryRecentSerialRedConditionDO);

    List<QueryRecentSerialRedOutPutDTO> queryRecentSerialGreen(@Param("condition") QueryRecentSerialRedConditionDO queryRecentSerialRedConditionDO);


    List<MarketOutputDomain> queryPrePurchaseSocker(QueryRecentSerialRedConditionDO input);

    List<MarketOutputDomain> queryPreFiveAndSubFiveSocker(QueryRecentSerialRedConditionDO input);

    List<MarketOutputDomain> queryFiveRatioByCodeAndDate(@Param("list") List<MarketOutputDomain> list);
}