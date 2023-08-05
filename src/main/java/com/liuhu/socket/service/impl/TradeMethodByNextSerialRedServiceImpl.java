package com.liuhu.socket.service.impl;

import com.liuhu.socket.common.DateUtils;
import com.liuhu.socket.common.MathConstants;
import com.liuhu.socket.dao.MarketInfoMapper;
import com.liuhu.socket.dao.MarketInfoNewMapper;
import com.liuhu.socket.dao.SerialTempMapper;
import com.liuhu.socket.dao.ShareInfoMapper;
import com.liuhu.socket.domain.input.GetRateThreeIncomeInputDTO;
import com.liuhu.socket.domain.input.QueryRecentSerialRedConditionDTO;
import com.liuhu.socket.domain.output.MarketRateTheeOutPutDTO;
import com.liuhu.socket.domain.output.QueryRecentSerialRedOutPutDTO;
import com.liuhu.socket.domain.output.RecentDownOrderOutPutDTO;
import com.liuhu.socket.dto.QueryRecentSerialRedConditionDO;
import com.liuhu.socket.entity.ShareInfo;
import com.liuhu.socket.service.TradeInfoService;
import com.liuhu.socket.service.TradeMethodService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component("nextSerialRed")
@Slf4j
public class TradeMethodByNextSerialRedServiceImpl implements TradeMethodService {

    @Autowired
    SerialTempMapper serialTempMapper;

    @Autowired
    TradeInfoService tradeInfoService;

    @Autowired
    MarketInfoNewMapper marketInfoNewMapper;

    @Autowired
    MarketInfoMapper marketInfoMapper;

    @Autowired
    ShareInfoMapper shareInfoMapper;

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
        List<String> shareCodeList = new ArrayList<>();
        if (input2Domain.getAllFlag()) {
            ShareInfo shareInfo = new ShareInfo();
            shareInfo.setHushenStatus("B");
            shareInfo.setStatus("1");
            List<ShareInfo> shareInfoList = shareInfoMapper.getShareInfo(shareInfo);
            shareCodeList = shareInfoList.stream().map(shareInfo1 -> shareInfo1.getShareCode()).collect(Collectors.toList());
        } else {
            shareCodeList.add(input2Domain.getShareCode());
        }
        for (String shareCode : shareCodeList) {
            input2Domain.setRateOrAmountDay(Integer.valueOf(shareCode));
            List<String> newShareCodeList = new ArrayList<>();
            newShareCodeList.add("cn_" + shareCode);
            List<QueryRecentSerialRedOutPutDTO> outPutDTOList = marketInfoNewMapper.queryThreeDownThen(null, newShareCodeList, input2Domain);
            if (outPutDTOList.size() > 0) {
                serialTempMapper.insertList(outPutDTOList, input2Domain.getRateOrAmountDay());
            }
        }
        return null;
    }

    @Override
    public MarketRateTheeOutPutDTO getRateThreeIncome(GetRateThreeIncomeInputDTO getRateThreeIncomeInputDTO) {
        MarketRateTheeOutPutDTO returnRateDTO = new MarketRateTheeOutPutDTO();
        List<RecentDownOrderOutPutDTO> orderOutPutDTOList = new ArrayList<>();

        Integer maxDoubleSize = 1;
        String maxShareCode = null;
        ShareInfo shareInfo = new ShareInfo();
        shareInfo.setStatus("1");
        shareInfo.setHushenStatus("B");
        String shareCode = getRateThreeIncomeInputDTO.getShareCode();
        if (Objects.nonNull(shareCode)){
            shareInfo.setShareCode(shareCode);
        }
        List<ShareInfo> shareInfoList = shareInfoMapper.getShareInfo(shareInfo);
        for (ShareInfo shareInfo1:shareInfoList){
            RecentDownOrderOutPutDTO  recentDownOrderOutPutDTO = new RecentDownOrderOutPutDTO();
            getRateThreeIncomeInputDTO.setType(shareInfo1.getShareCode());
            List<QueryRecentSerialRedOutPutDTO> minRateThreeList = serialTempMapper.getMinRateThree(getRateThreeIncomeInputDTO);
            if (minRateThreeList.size() == 0) {
                return returnRateDTO;
            }
            //最终收益率
            Double tFinalRatio = 0.0;
            Double tAllFinalRatio = 0.0;
            //翻倍次数
            Integer doubleSize=1;
            //实时收益率
            Double runRatio = 0.0;
            //基金净值 开始计算设置为1
            Double unitPrice = 1.0;
            //购买单元
            int unitAmount;
            Double allProfit=0.0;
            for (QueryRecentSerialRedOutPutDTO queryRecentSerialRedOutPutDTO : minRateThreeList) {
                shareCode= queryRecentSerialRedOutPutDTO.getShareCode();
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
                    unitProfit =  (regularProfit - runRatio) * unitAmount * 0.01 - unitAmount*doubleSize*fee*0.01;
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
            if (maxDoubleSize<doubleSize){
                maxDoubleSize = doubleSize;
                maxShareCode =shareCode;
            }
            tAllFinalRatio = tAllFinalRatio + tFinalRatio;
            returnRateDTO.setIncome(tAllFinalRatio);
            returnRateDTO.setAmount(allProfit);
            returnRateDTO.setPeriodRatio(unitPrice - 1);
            recentDownOrderOutPutDTO.setDoubleSize(doubleSize);
            recentDownOrderOutPutDTO.setFinalProfit(allProfit);
            recentDownOrderOutPutDTO.setRunRatio(runRatio);
            recentDownOrderOutPutDTO.setShareCode(shareCode);
            orderOutPutDTOList.add(recentDownOrderOutPutDTO);
        }
        returnRateDTO.setMaxDoubleSize(maxDoubleSize);
        returnRateDTO.setMaxShareCode(maxShareCode);
        List<RecentDownOrderOutPutDTO> collect = orderOutPutDTOList.stream().sorted(Comparator.comparingInt(RecentDownOrderOutPutDTO::getDoubleSize)).collect(Collectors.toList());
        returnRateDTO.setOrderOutPutDTOList(collect);
        return returnRateDTO;
    }

    @Override
    public List<QueryRecentSerialRedOutPutDTO> queryThreeUpThenAndPreDownRegular(QueryRecentSerialRedConditionDTO input) {
        return null;
    }

    /**
     * 查询最近一段时间第一次买入最多及接下来的行情
     * @param getRateThreeIncomeInputDTO
     * @return
     */
    @Override
    public List<QueryRecentSerialRedOutPutDTO> queryFirstBuyMoreThenMarketRatio(GetRateThreeIncomeInputDTO getRateThreeIncomeInputDTO) {

        List<String> queryPeriodList = new ArrayList<>();
        Integer queryPeriod = getRateThreeIncomeInputDTO.getQueryPeriod();
        if (Objects.nonNull(queryPeriod)&&queryPeriod>1){
            List<Date> dateList = tradeInfoService.queryPeriodDateList(getRateThreeIncomeInputDTO.getQueryStartTime(), queryPeriod, null);
            for (Date date:dateList){
                queryPeriodList.add(DateUtils.format(date,DateUtils.DateFormat.YYYY_MM_DD_HH_MM_SS));
            }
        }else {
            queryPeriodList.add(getRateThreeIncomeInputDTO.getQueryStartTime());
        }
        List<QueryRecentSerialRedOutPutDTO> respList = new ArrayList<>();
        for (String dateStr:queryPeriodList){
            getRateThreeIncomeInputDTO.setQueryStartTime(dateStr);
            List<QueryRecentSerialRedOutPutDTO> outPutDTOList = marketInfoNewMapper.queryHeadRatioShareInfo(getRateThreeIncomeInputDTO);
            if (outPutDTOList.size()<=0){
                continue;
            }
            List<String> shareCodeList = outPutDTOList.stream().map(QueryRecentSerialRedOutPutDTO::getShareCode).collect(Collectors.toList());
            QueryRecentSerialRedConditionDO queryRecentSerialRedConditionDO = new QueryRecentSerialRedConditionDO();
            String queryStartTime = getRateThreeIncomeInputDTO.getQueryStartTime();
            Date endTime = DateUtils.parse(queryStartTime, DateUtils.DateFormat.YYYY_MM_DD_HH_MM_SS);
            Date preTime = tradeInfoService.getWantDate(getRateThreeIncomeInputDTO.getRecentDay(), endTime, "sub");
            queryRecentSerialRedConditionDO.setDownStartTime(preTime);
            queryRecentSerialRedConditionDO.setDownEndTime(endTime);
            queryRecentSerialRedConditionDO.setMinDownRate(getRateThreeIncomeInputDTO.getMinDownRate()*(-2));
            queryRecentSerialRedConditionDO.setShareCodeList(shareCodeList);
            queryRecentSerialRedConditionDO.setMinDownDay(getRateThreeIncomeInputDTO.getMinDownDay());
            List<QueryRecentSerialRedOutPutDTO> recentList = marketInfoMapper.queryRecentSerialGreen(queryRecentSerialRedConditionDO);
            if (recentList.size()<=0){
                continue;
            }
            shareCodeList = recentList.stream().map(QueryRecentSerialRedOutPutDTO::getShareCode).collect(Collectors.toList());
            queryRecentSerialRedConditionDO.setShareCodeList(shareCodeList);
            queryRecentSerialRedConditionDO.setSelectStartTime(tradeInfoService.getWantDate(1, endTime, "plus"));
            queryRecentSerialRedConditionDO.setSelectEndTime(tradeInfoService.getWantDate(1, endTime, "plus"));
            queryRecentSerialRedConditionDO.setRecentRateDay(1);
            List<QueryRecentSerialRedOutPutDTO> outPutDTOList1 = marketInfoMapper.queryRecentSerialRedWithHavingShareCode(queryRecentSerialRedConditionDO);
            if (outPutDTOList1.size()>0){
                respList.addAll(outPutDTOList1);
            }
        }
        serialTempMapper.insertList(respList,getRateThreeIncomeInputDTO.getRowNum());
        return new ArrayList<>();
    }

}
