package com.liuhu.socket.service.impl;

import com.liuhu.socket.dao.SerialTempMapper;
import com.liuhu.socket.domain.input.QueryRecentSerialRedConditionDTO;
import com.liuhu.socket.domain.output.MarketRateTheeOutPutDTO;
import com.liuhu.socket.domain.output.QueryRecentSerialRedOutPutDTO;
import com.liuhu.socket.service.TradeMethodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component("oneSockerIncome")
public class TradeMethodByOneSockerIncomeServiceImpl implements TradeMethodService {

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

    /**
     * 根据单倍收益阶梯式的翻倍操作
     * @param type
     * @return
     */
    @Override
    public MarketRateTheeOutPutDTO getRateThreeIncome(Integer type) {
        MarketRateTheeOutPutDTO returnRateDTO = new MarketRateTheeOutPutDTO();
        List<QueryRecentSerialRedOutPutDTO> minRateThreeList = serialTempMapper.getMinRateThree(type);
        if (minRateThreeList.size() == 0) {
            return returnRateDTO;
        }
        Double tIncome = 0.0;
        Map<Integer,Double> allIncomeMap = new HashMap<>();
        Double  passAllIncome = 0.0;
        Integer runDoubleUnit = 1;
        for (QueryRecentSerialRedOutPutDTO queryRecentSerialRedOutPutDTO : minRateThreeList) {
            Double maxRatio = queryRecentSerialRedOutPutDTO.getMaxRatio();
            Double finalRatio = queryRecentSerialRedOutPutDTO.getFinalRatio();
            if (allIncomeMap.isEmpty()){
                if (maxRatio>4){
                    tIncome = tIncome+4;
                    continue;
                }else if (finalRatio>0){
                    tIncome = tIncome+finalRatio;
                    continue;
                }
                allIncomeMap.put(1,finalRatio);
                continue;
            }
            Set<Integer> keySet = allIncomeMap.keySet();
            if (keySet.size()==1&&tIncome>50){
                returnRateDTO.setIncome(tIncome);
                return returnRateDTO;
            }
            //最大倍数 也可以当size来使用
            Integer maxDouble = keySet.stream().reduce(0, Integer::max);
            Collection<Double> values = allIncomeMap.values();
            Double allSubIncome = values.stream().reduce(0.0, Double::sum);
            //获取当前运行的成本翻倍数
            runDoubleUnit =Math.abs((int) (allSubIncome/10))+1;
            //最大翻倍 对应的收益
            Map<Integer, Double> sortedMap = new TreeMap<>(Collections.reverseOrder());
            sortedMap.putAll(allIncomeMap);
           // int i =1;
           //  int tempDoubleUnit = runDoubleUnit;
           for (Map.Entry entry:sortedMap.entrySet()){
               Integer key = (Integer) entry.getKey();
               Double income = (Double)entry.getValue();
               int baseDouble = runDoubleUnit - maxDouble + 1;
               if (baseDouble*maxRatio+income>=4){
                   allIncomeMap.remove(key);
                   runDoubleUnit--;
                   tIncome = tIncome +4;
                   continue;
               }
               income = income + finalRatio;
               allIncomeMap.put(key,income);
           }
           if (runDoubleUnit>maxDouble){
               for (int i = runDoubleUnit;i>maxDouble;i--){
                   allIncomeMap.put(i,finalRatio);
               }
           }

        }
        returnRateDTO.setIncome(tIncome);
        returnRateDTO.setAllIncomeMap(allIncomeMap);
        return returnRateDTO;
    }
}
