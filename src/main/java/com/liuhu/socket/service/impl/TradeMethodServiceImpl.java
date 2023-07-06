package com.liuhu.socket.service.impl;

import com.liuhu.socket.common.DateUtils;
import com.liuhu.socket.dao.SerialTempMapper;
import com.liuhu.socket.domain.input.QueryRecentSerialRedConditionDTO;
import com.liuhu.socket.domain.output.QueryRecentSerialRedOutPutDTO;
import com.liuhu.socket.dto.QueryRecentSerialRedConditionDO;
import com.liuhu.socket.service.TradeInfoService;
import com.liuhu.socket.service.TradeMethodService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class TradeMethodServiceImpl  implements TradeMethodService {

    @Resource
    SerialTempMapper serialTempMapper;

    @Resource
    TradeInfoService tradeInfoService;

    /**
     * 执行如果连red三次则结束
     * @param input2Domain
     * @return
     */
    @Override
    public List<QueryRecentSerialRedOutPutDTO> getRecentFinalRatioStrategy(QueryRecentSerialRedConditionDTO input2Domain){
        QueryRecentSerialRedConditionDO queryRecentSerialRedConditionDO = new QueryRecentSerialRedConditionDO();
        queryRecentSerialRedConditionDO.setSelectStartTime(DateUtils.parse(input2Domain.getSelectStartTime(), DateUtils.DateFormat.YYYY_MM_DD_HH_MM_SS));
        queryRecentSerialRedConditionDO.setSelectEndTime(DateUtils.parse(input2Domain.getSelectEndTime(), DateUtils.DateFormat.YYYY_MM_DD_HH_MM_SS));
        BeanUtils.copyProperties(input2Domain,queryRecentSerialRedConditionDO);
         List<QueryRecentSerialRedOutPutDTO> outPutDTOList = serialTempMapper.getResultByCondition(queryRecentSerialRedConditionDO);
         if (outPutDTOList.size()==0){
             return null;
         }
         List<QueryRecentSerialRedOutPutDTO>  redThreeList  =  serialTempMapper.getRecentFinalRatioRedThree(outPutDTOList,queryRecentSerialRedConditionDO);

         serialTempMapper.insertList(redThreeList,10);

         return redThreeList;
     }
}
