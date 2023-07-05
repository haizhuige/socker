package com.liuhu.socket.dao;

import com.liuhu.socket.domain.input.MarketInput2Domain;
import com.liuhu.socket.domain.input.QueryRecentSerialRedConditionDTO;
import com.liuhu.socket.domain.output.QueryRecentSerialRedOutPutDTO;
import com.liuhu.socket.dto.QueryRecentSerialRedConditionDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SerialTempMapper {

    int insertList(@Param("list") List<QueryRecentSerialRedOutPutDTO> list,@Param("hi") Integer hi);

    List<QueryRecentSerialRedOutPutDTO> getResultByCondition(QueryRecentSerialRedConditionDO marketInput2Domain);

    List<QueryRecentSerialRedOutPutDTO> getRecentFinalRatioRedThree(@Param("list") List<QueryRecentSerialRedOutPutDTO> outPutDTOList,@Param("input")QueryRecentSerialRedConditionDO marketInput2Domain);
}