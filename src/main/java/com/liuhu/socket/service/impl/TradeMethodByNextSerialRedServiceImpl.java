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
        for (QueryRecentSerialRedOutPutDTO queryRecentSerialRedOutPutDTO : minRateThreeList) {

            Integer cycleProfit = getRateThreeIncomeInputDTO.getCycleProfit();
            if (Objects.nonNull(cycleProfit) && tFinalRatio > getRateThreeIncomeInputDTO.getCycleProfit() && runRatio < getRateThreeIncomeInputDTO.getRunRatio()) {
                runRatio = 0.0;
                tAllFinalRatio = tAllFinalRatio + tFinalRatio;
                tFinalRatio = 0.0;
            }
            Double finalRatio = queryRecentSerialRedOutPutDTO.getFinalRatio();
            Double maxRatio = queryRecentSerialRedOutPutDTO.getMaxRatio();
            Double minRatio = queryRecentSerialRedOutPutDTO.getMinRatio();
            //获取当前总收益
            int i = Math.abs((int) (runRatio / getRateThreeIncomeInputDTO.getDoubleSize())) + 1;
            log.info("doubleSize：{},date：{},tfinal：{},runRatio：{},finalRatio:{},maxRatio:{}", i, queryRecentSerialRedOutPutDTO.getStartTime(), MathConstants.Pointkeep(tFinalRatio, 2), MathConstants.Pointkeep(runRatio, 2), MathConstants.Pointkeep(finalRatio, 2), MathConstants.Pointkeep(maxRatio, 2));
            doubleSize = i;
            if (doubleSize == 1) {
                if (minRatio + runRatio > getRateThreeIncomeInputDTO.getProfit()) {
                    tFinalRatio = tFinalRatio + minRatio - getRateThreeIncomeInputDTO.getFee();//减掉0.2 的sxf
                    runRatio = 0.0;
                    continue;
                } else if (maxRatio + runRatio > getRateThreeIncomeInputDTO.getProfit()) {
                    tFinalRatio = tFinalRatio - runRatio + getRateThreeIncomeInputDTO.getProfit() - getRateThreeIncomeInputDTO.getFee();//减掉0.2 的sxf
                    runRatio = 0.0;
                    continue;
                } else {
                    tFinalRatio = tFinalRatio + finalRatio;//减掉0.2 的sxf
                    runRatio = runRatio + finalRatio;
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
                    break;
                case 3:
                    regularProfit = getRateThreeIncomeInputDTO.getDoubleProfit() - 0.2;
                    break;
                case 4:
                    regularProfit = getRateThreeIncomeInputDTO.getDoubleProfit() - 0.3;
                    break;
                case 5:
                    regularProfit = getRateThreeIncomeInputDTO.getDoubleProfit() - 0.5;
                    break;
                default:
                    regularProfit = getRateThreeIncomeInputDTO.getDoubleProfit() - 0.7;
            }


            if (minRatio + runRatio > regularProfit) {
                tFinalRatio = tFinalRatio + minRatio * doubleSize - runRatio - getRateThreeIncomeInputDTO.getFee();
                runRatio = 0.0;
                continue;
            } else if (maxRatio + runRatio > regularProfit) {
                tFinalRatio = tFinalRatio + (regularProfit - runRatio) * doubleSize - getRateThreeIncomeInputDTO.getFee();
                runRatio = 0.0;
                continue;
            } else {
                tFinalRatio = tFinalRatio + finalRatio * doubleSize;
                runRatio = runRatio + finalRatio;
                continue;
            }


        }
        tAllFinalRatio = tAllFinalRatio + tFinalRatio;
        returnRateDTO.setIncome(tAllFinalRatio);
        return returnRateDTO;
    }

    @Override
    public List<QueryRecentSerialRedOutPutDTO> queryThreeUpThenAndPreDownRegular(QueryRecentSerialRedConditionDTO input) {
        return null;
    }
}
