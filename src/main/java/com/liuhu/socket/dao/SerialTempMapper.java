package com.liuhu.socket.dao;

import com.liuhu.socket.domain.input.GetRateThreeIncomeInputDTO;
import com.liuhu.socket.domain.input.QueryFixSerialDownInDTO;
import com.liuhu.socket.domain.output.QueryFixSerialDownOutDTO;
import com.liuhu.socket.domain.output.QueryRecentSerialRedOutPutDTO;
import com.liuhu.socket.dto.QueryRecentSerialRedConditionDO;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface SerialTempMapper {

    int insertList(@Param("list") List<QueryRecentSerialRedOutPutDTO> list,@Param("hi") Integer hi);

    List<QueryRecentSerialRedOutPutDTO> getResultByCondition(QueryRecentSerialRedConditionDO marketInput2Domain);

    List<QueryRecentSerialRedOutPutDTO> getRecentFinalRatioRedThree(@Param("list") List<QueryRecentSerialRedOutPutDTO> outPutDTOList,@Param("input")QueryRecentSerialRedConditionDO marketInput2Domain);

    List<QueryRecentSerialRedOutPutDTO> getMinRateThree(@Param("domain") GetRateThreeIncomeInputDTO rateThreeIncomeInputDTO);
    //从处理过的数据获取最大日期
    Date selectMaxDateFromHandleData(@Param("shareCode")String newShareCode);

    List<QueryFixSerialDownOutDTO> queryShareInfoByCondition(@Param("condition")QueryFixSerialDownInDTO conditionDTO);
}