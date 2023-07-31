package com.liuhu.socket.service.impl;

import com.liuhu.socket.common.MathConstants;
import com.liuhu.socket.dao.MarketInfoNewMapper;
import com.liuhu.socket.dao.SerialTempMapper;
import com.liuhu.socket.domain.input.GetRateThreeIncomeInputDTO;
import com.liuhu.socket.domain.input.QueryRecentSerialRedConditionDTO;
import com.liuhu.socket.domain.output.MarketRateTheeOutPutDTO;
import com.liuhu.socket.domain.output.QueryRecentSerialRedOutPutDTO;
import com.liuhu.socket.service.TradeInfoService;
import com.liuhu.socket.service.TradeMethodService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component("nextSerialRed")
@Slf4j
public class TradeMethodByNextSerialRedServiceImpl implements TradeMethodService {

    @Autowired
    SerialTempMapper serialTempMapper;

    @Autowired
    TradeInfoService tradeInfoService;

    @Autowired
    MarketInfoNewMapper marketInfoNewMapper;

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

        String startTime = input2Domain.getStartTime();
        if (Objects.isNull(startTime)) {
            throw new Exception("截至日期不能为空");
        }
        List<String> shareCodeList = new ArrayList<>();
        shareCodeList.add(input2Domain.getShareCode());
        List<QueryRecentSerialRedOutPutDTO> outPutDTOList =  marketInfoNewMapper.queryThreeDownThen(null,shareCodeList,input2Domain);
        if (outPutDTOList.size()>0){
            serialTempMapper.insertList(outPutDTOList,input2Domain.getRateOrAmountDay());
        }
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
        Double tAllFinalRatio = 0.0;
        //翻倍次数
        Integer doubleSize;
        //实时收益率
        Double runRatio = 0.0;
        //基金净值 开始计算设置为1
        Double unitPrice = 1.0;
        //购买单元
        int unitAmount;
        Double allProfit=0.0;
        for (QueryRecentSerialRedOutPutDTO queryRecentSerialRedOutPutDTO : minRateThreeList) {

         /*   Date startTime = queryRecentSerialRedOutPutDTO.getStartTime();
            String dateStr = DateUtils.format(startTime, DateUtils.DateFormat.YYYY_MM_DD_HH_MM_SS);
            if ("2021-06-16 00:00:00".equals(dateStr)){
                System.out.println("1111111111111");
            }*/
            Integer cycleProfit = getRateThreeIncomeInputDTO.getCycleProfit();
            if (Objects.nonNull(cycleProfit) && tFinalRatio > getRateThreeIncomeInputDTO.getCycleProfit() && runRatio < getRateThreeIncomeInputDTO.getRunRatio()) {
                runRatio = 0.0;
                tAllFinalRatio = tAllFinalRatio + tFinalRatio;
                tFinalRatio = 0.0;
                unitPrice = 1.0;
            }
            Double unitProfit = 0.0;//当天的收益
            Double unitPriceTemp = unitPrice;
            Double finalRatio = queryRecentSerialRedOutPutDTO.getFinalRatio();
            Double maxRatio = queryRecentSerialRedOutPutDTO.getMaxRatio();
            Double minRatio = queryRecentSerialRedOutPutDTO.getMinRatio();
            Double fee = getRateThreeIncomeInputDTO.getFee();
            Double profit = getRateThreeIncomeInputDTO.getProfit();
            unitPrice = unitPrice*(1+finalRatio*0.01);
            //获取当前总收益
            int i = Math.abs((int) (runRatio / getRateThreeIncomeInputDTO.getDoubleSize())) + 1;
            log.info("doubleSize：{},date：{},tfinal：{},runRatio：{},finalRatio:{},maxRatio:{},allProfit:{}", i, queryRecentSerialRedOutPutDTO.getStartTime(), MathConstants.Pointkeep(tFinalRatio, 2), MathConstants.Pointkeep(runRatio, 2), MathConstants.Pointkeep(finalRatio, 2), MathConstants.Pointkeep(maxRatio, 2),allProfit);
            doubleSize = i;
            unitAmount = 10000* doubleSize;
            if (doubleSize == 1) {
                if (minRatio + runRatio > profit) {
                    tFinalRatio = tFinalRatio + minRatio - fee;//减掉0.2 的sxf
                    unitProfit =  minRatio * unitAmount * 0.01 - unitAmount * fee*0.01;
                    allProfit = allProfit +unitProfit;
                    runRatio = 0.0;
                    continue;
                } else if (maxRatio + runRatio > profit) {
                    tFinalRatio = tFinalRatio - runRatio + profit - fee;//减掉0.2 的sxf
                    unitProfit =  (profit - runRatio) * unitAmount * 0.01 - unitAmount * fee*0.01;
                    allProfit = allProfit +unitProfit;
                    runRatio = 0.0;
                    continue;
                } else {
                    tFinalRatio = tFinalRatio + finalRatio;//减掉0.2 的sxf
                    runRatio = runRatio + finalRatio;
                    unitProfit =  finalRatio * unitAmount * 0.01;
                    allProfit = allProfit +unitProfit;
                    continue;
                }

            }

            double regularProfit;
            switch (doubleSize) {
                case 1:
                    regularProfit = getRateThreeIncomeInputDTO.getDoubleProfit();
                    break;
                case 2:
                    regularProfit = getRateThreeIncomeInputDTO.getDoubleProfit();
                    fee = fee *0.6;
                    break;
                case 3:
                    regularProfit = getRateThreeIncomeInputDTO.getDoubleProfit() - 0.2;
                    fee = fee *0.5;
                    break;
                case 4:
                    regularProfit = getRateThreeIncomeInputDTO.getDoubleProfit() - 0.3;
                    fee = fee *0.4;
                    break;
                case 5:
                    regularProfit = getRateThreeIncomeInputDTO.getDoubleProfit() - 0.5;
                    fee = fee *0.3;
                    break;
                default:
                    regularProfit = getRateThreeIncomeInputDTO.getDoubleProfit() - 0.6;
                    fee = fee *0.3;
            }


            if (minRatio + runRatio > regularProfit) {
                tFinalRatio = tFinalRatio + minRatio * doubleSize - runRatio - fee;
                unitProfit =  minRatio * unitAmount * 0.01 - unitAmount * fee*0.01;
                allProfit = allProfit +unitProfit;
                runRatio = 0.0;
                continue;
            } else if (maxRatio + runRatio > regularProfit) {
                tFinalRatio = tFinalRatio + (regularProfit - runRatio) * doubleSize - doubleSize * fee;
                unitProfit =  (regularProfit - runRatio) * unitAmount * 0.01 - unitAmount*doubleSize*fee*0.01;;
                allProfit = allProfit +unitProfit;
                runRatio = 0.0;
                continue;
            } else {
                tFinalRatio = tFinalRatio + finalRatio * doubleSize;
                runRatio = runRatio + finalRatio;
                unitProfit =  finalRatio * unitAmount * 0.01;
                allProfit = allProfit +unitProfit;
                continue;
            }


        }
        tAllFinalRatio = tAllFinalRatio + tFinalRatio;
        returnRateDTO.setIncome(tAllFinalRatio);
        returnRateDTO.setAmount(allProfit);
        returnRateDTO.setPeriodRatio(unitPrice - 1);
        return returnRateDTO;
    }

    @Override
    public List<QueryRecentSerialRedOutPutDTO> queryThreeUpThenAndPreDownRegular(QueryRecentSerialRedConditionDTO input) {
        return null;
    }

}
