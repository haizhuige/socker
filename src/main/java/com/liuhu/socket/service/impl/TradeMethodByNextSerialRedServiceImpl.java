package com.liuhu.socket.service.impl;

import com.liuhu.socket.common.MathConstants;
import com.liuhu.socket.dao.SerialTempMapper;
import com.liuhu.socket.domain.input.GetRateThreeIncomeInputDTO;
import com.liuhu.socket.domain.input.QueryRecentSerialRedConditionDTO;
import com.liuhu.socket.domain.output.MarketRateTheeOutPutDTO;
import com.liuhu.socket.domain.output.QueryRecentSerialRedOutPutDTO;
import com.liuhu.socket.service.TradeMethodService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("nextSerialRed")
@Slf4j
public class TradeMethodByNextSerialRedServiceImpl implements TradeMethodService {

    @Autowired
    SerialTempMapper serialTempMapper;

    @Override
    public List<QueryRecentSerialRedOutPutDTO> getRecentFinalRatioStrategy(QueryRecentSerialRedConditionDTO marketInput2Domain) {
        return null;
    }

    @Override
    public List<QueryRecentSerialRedOutPutDTO> queryVRatioFromDownStartPoint(QueryRecentSerialRedConditionDTO input2Domain) throws Exception {
        return null;
    }

    @Override
    public List<QueryRecentSerialRedOutPutDTO> queryThreeDownRatioByDate(QueryRecentSerialRedConditionDTO input2Domain) throws Exception {
        return null;
    }

    @Override
    public MarketRateTheeOutPutDTO getRateThreeIncome(GetRateThreeIncomeInputDTO getRateThreeIncomeInputDTO) {
        MarketRateTheeOutPutDTO returnRateDTO = new MarketRateTheeOutPutDTO();
        List<QueryRecentSerialRedOutPutDTO> minRateThreeList = serialTempMapper.getMinRateThree(getRateThreeIncomeInputDTO);
        if (minRateThreeList.size() == 0) {
            return returnRateDTO;
        }
        //最终收益率
        Double tFinalRatio = 0.0;
        //翻倍次数
        Integer doubleSize =1;
        //实时收益率
        Double runRatio = 0.0;
        for (QueryRecentSerialRedOutPutDTO queryRecentSerialRedOutPutDTO : minRateThreeList) {
                if (tFinalRatio>getRateThreeIncomeInputDTO.getCycleProfit()){
                    runRatio =0.0;
                }
              Double finalRatio = queryRecentSerialRedOutPutDTO.getFinalRatio();
                Double maxRatio = queryRecentSerialRedOutPutDTO.getMaxRatio();
               Double minRatio = queryRecentSerialRedOutPutDTO.getMinRatio();
            //获取当前总收益
            int i = Math.abs((int) (runRatio /getRateThreeIncomeInputDTO.getDoubleSize())) + 1;
            log.info("doubleSize：{},date：{},tfinal：{},runRatio：{},finalRatio:{}",i,queryRecentSerialRedOutPutDTO.getStartTime(), MathConstants.Pointkeep(tFinalRatio,2),MathConstants.Pointkeep(runRatio,2),MathConstants.Pointkeep(finalRatio,2));
            doubleSize = i;
               if (doubleSize==1){
                   if (minRatio+runRatio>1.2){
                       tFinalRatio = tFinalRatio+minRatio-0.2;//减掉0.2 的sxf
                       runRatio =0.0;
                       continue;
                   }else if (maxRatio+runRatio>1.2){
                       tFinalRatio = tFinalRatio-runRatio+1;//减掉0.2 的sxf
                       runRatio =0.0;
                       continue;
                   }else {
                       tFinalRatio = tFinalRatio+finalRatio-0.2;//减掉0.2 的sxf
                       runRatio = runRatio +finalRatio-0.2;
                       continue;
                   }

               }

               double regularProfit;
               switch (doubleSize){
                   case 1:
                       regularProfit = 1.5;
                       break;
                   case 2:
                       regularProfit = 1.5;
                       break;
                   case 3:
                       regularProfit = 1.3;
                       break;
                   case 4:
                       regularProfit = 1.1;
                       break;
                   case 5:
                       regularProfit = 1.0;
                       break;
                       default:regularProfit= 0.8;
               }


               if (minRatio+runRatio>regularProfit){
                   tFinalRatio = tFinalRatio+minRatio*doubleSize-0.2*doubleSize;
                   runRatio =0.0;
                   continue;
               }else if (maxRatio+runRatio>regularProfit){
                   tFinalRatio = tFinalRatio+regularProfit*doubleSize-runRatio*doubleSize;
                   runRatio =0.0;
                   continue;
               }else {
                   tFinalRatio = tFinalRatio+finalRatio*doubleSize-0.2*doubleSize;
                   runRatio = runRatio +finalRatio- 0.2;
                   continue;
               }


        }
        returnRateDTO.setIncome(tFinalRatio);
        return returnRateDTO;
    }
}
