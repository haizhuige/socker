package com.liuhu.socket.service.impl;

import com.liuhu.socket.common.DateUtils;
import com.liuhu.socket.common.MathConstants;
import com.liuhu.socket.dao.MarketInfoMapper;
import com.liuhu.socket.dao.MarketInfoNewMapper;
import com.liuhu.socket.dao.SerialTempMapper;
import com.liuhu.socket.dao.ShareInfoMapper;
import com.liuhu.socket.dao.TradeDateMapper;
import com.liuhu.socket.domain.input.GetRateThreeIncomeInputDTO;
import com.liuhu.socket.domain.input.MarketInputDomain;
import com.liuhu.socket.domain.input.QueryRecentSerialRedConditionDTO;
import com.liuhu.socket.domain.input.QueryStockReturnDTO;
import com.liuhu.socket.domain.output.MarketRateTheeOutPutDTO;
import com.liuhu.socket.domain.output.MarketRealTimeOutputDomain;
import com.liuhu.socket.domain.output.QueryRecentSerialRedOutPutDTO;
import com.liuhu.socket.domain.output.RecentDownOrderOutPutDTO;
import com.liuhu.socket.dto.QueryRecentSerialRedConditionDO;
import com.liuhu.socket.entity.ShareInfo;
import com.liuhu.socket.service.SharesInfoService;
import com.liuhu.socket.service.TradeInfoService;
import com.liuhu.socket.service.TradeMethodService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Component("nextSerialRed")
@Slf4j
public class TradeMethodByNextSerialRedServiceImpl implements TradeMethodService {

    @Autowired
    SerialTempMapper serialTempMapper;

    @Autowired
    TradeInfoService tradeInfoService;

    @Autowired
    SharesInfoService sharesInfoService;

    @Autowired
    MarketInfoNewMapper marketInfoNewMapper;

    @Autowired
    MarketInfoMapper marketInfoMapper;

    @Autowired
    ShareInfoMapper shareInfoMapper;

    @Autowired
    TradeDateMapper tradeDateMapper;


    @Qualifier("taskExecutor")
    @Autowired
    ThreadPoolTaskExecutor taskExecutor;

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
        // delegated to optimized calculateStockReturn method
        QueryStockReturnDTO inputDTO = new QueryStockReturnDTO();
        inputDTO.setAllFlag(input2Domain.getAllFlag());
        inputDTO.setShareCode(input2Domain.getShareCode());
        inputDTO.setStartTime(input2Domain.getStartTime());
        inputDTO.setEndTime(input2Domain.getEndTime());
        inputDTO.setHushenStatus(input2Domain.getHushenStatus());
        inputDTO.setRateOrAmountDay(input2Domain.getRateOrAmountDay());
        return calculateStockReturn(inputDTO);
    }

    /**
     * 计算股票收益率并存储到 serial_temp 表（优化版）
     */
    @Override
    public List<QueryRecentSerialRedOutPutDTO> calculateStockReturn(QueryStockReturnDTO inputDTO) throws Exception {
        List<String> shareCodeList = prepareShareCodeList(inputDTO);
        if (CollectionUtils.isEmpty(shareCodeList)) {
            log.warn("未找到符合条件的股票列表");
            return new ArrayList<>();
        }

        int totalSize = shareCodeList.size();
        int threadPoolSize = Math.min(40, Math.max(20, totalSize / 125));
        CountDownLatch latch = new CountDownLatch(totalSize);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        log.info("开始计算股票收益率，总数:{}, 线程池大小:{}", totalSize, threadPoolSize);

        for (String shareCode : shareCodeList) {
            String finalShareCode = shareCode;
            taskExecutor.execute(() -> {
                try {
                    executeSingleShare(finalShareCode, inputDTO);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                    log.error("计算股票收益率失败，shareCode:{}, e:{}", finalShareCode, e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        // 等待所有任务完成，动态计算超时时间
        long timeout = Math.max(300, totalSize / threadPoolSize * 2);
        boolean completed = latch.await(timeout, TimeUnit.SECONDS);

        if (!completed) {
            log.warn("计算股票收益率超时，timeout:{}s, 剩余任务数:{}", timeout, latch.getCount());
        }

        log.info("计算股票收益率完成，总数:{}, 成功:{}, 失败:{}, 耗时:{}s",
                totalSize, successCount.get(), failureCount.get(), timeout);
        return null;
    }

    /**
     * 准备股票代码列表
     */
    private List<String> prepareShareCodeList(QueryStockReturnDTO inputDTO) throws Exception {
        if (Boolean.TRUE.equals(inputDTO.getAllFlag())) {
            ShareInfo shareInfo = new ShareInfo();
            shareInfo.setHushenStatus(inputDTO.getHushenStatus());
            shareInfo.setStatus("1");
            List<ShareInfo> shareInfoList = shareInfoMapper.getShareInfo(shareInfo);
            if (CollectionUtils.isEmpty(shareInfoList)) {
                return new ArrayList<>();
            }

            // 检查是否已处理过最新日期
            String shareCode = shareInfoList.get(0).getShareCode();
            Date maxDate = serialTempMapper.selectMaxDateFromHandleData("cn_"+shareCode);
            Date marketNewDate = tradeInfoService.queryMaxDate().getDate();
            if (maxDate != null && maxDate.compareTo(marketNewDate) == 0) {
                log.info("数据已处理到最新日期:{}, 跳过", marketNewDate);
                return new ArrayList<>();
            }

            return shareInfoList.stream()
                    .map(ShareInfo::getShareCode)
                    .collect(Collectors.toList());
        } else {
            String shareCode = inputDTO.getShareCode();
            if (shareCode == null || shareCode.isEmpty()) {
                throw new IllegalArgumentException("shareCode 不能为空");
            }
            return Collections.singletonList(shareCode);
        }
    }

    /**
     * 执行单只股票收益率计算
     */
    private void executeSingleShare(String shareCode, QueryStockReturnDTO inputDTO) {
        try {
            List<String> newShareCodeList = Collections.singletonList("cn_" + shareCode);

            // 如果没有指定时间范围，取 serial_temp 中该股票已处理的最大日期，然后取下一个交易日
            String startTime = inputDTO.getStartTime();
            if (startTime == null) {
                // 从 serial_temp 表查询该股票已处理的最大日期
                Date maxHandleDate = serialTempMapper.selectMaxDateFromHandleData("cn_" + shareCode);

                Date nextTradeDate;
                if (maxHandleDate != null) {
                    // 取已处理日期之后的第一个交易日
                    nextTradeDate = tradeDateMapper.getNextTradeDate(maxHandleDate);
                } else {
                    // 如果没有处理过，取当前日期前一天的交易日
                    Date yesterday = tradeInfoService.getWantDate(1, new Date(), "sub");
                    nextTradeDate = tradeDateMapper.getNextTradeDate(yesterday);
                }

                if (nextTradeDate != null) {
                    startTime = DateUtils.format(nextTradeDate, DateUtils.DateFormat.YYYY_MM_DD_HH_MM_SS);
                } else {
                    log.warn("未找到下一个交易日，shareCode:{}", shareCode);
                    return;
                }
            }

            QueryRecentSerialRedConditionDTO conditionDTO = new QueryRecentSerialRedConditionDTO();
            conditionDTO.setStartTime(startTime);
            conditionDTO.setEndTime(inputDTO.getEndTime());

            // 查询收益率数据
            List<QueryRecentSerialRedOutPutDTO> outPutDTOList = marketInfoNewMapper.queryThreeDownThen(
                    null, newShareCodeList, conditionDTO);

            if (!CollectionUtils.isEmpty(outPutDTOList)) {
                // 插入到 serial_temp 表
                serialTempMapper.insertList(outPutDTOList, inputDTO.getRateOrAmountDay());
            }
        } catch (Exception e) {
            log.error("执行单只股票收益率计算失败，shareCode:{}, e:{}", shareCode, e.getMessage());
            throw new RuntimeException("执行失败", e);
        }
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
            //基金净值 开始计算设置为 1
            Double unitPrice = 1.0;
            //购买单元
            int unitAmount;
            Double allProfit=0.0;
            for (QueryRecentSerialRedOutPutDTO queryRecentSerialRedOutPutDTO : minRateThreeList) {
                shareCode= queryRecentSerialRedOutPutDTO.getShareCode();
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
                        tFinalRatio = tFinalRatio + minRatio - fee;//减掉 0.2 的 sxf
                        unitProfit =  minRatio * unitAmount * 0.01 - unitAmount * fee*0.01;
                        allProfit = allProfit +unitProfit;
                        runRatio = 0.0;
                        continue;
                    } else if (maxRatio + runRatio > profit) {
                        tFinalRatio = tFinalRatio - runRatio + profit - fee;//减掉 0.2 的 sxf
                        unitProfit =  (profit - runRatio) * unitAmount * 0.01 - unitAmount * fee*0.01;
                        allProfit = allProfit +unitProfit;
                        runRatio = 0.0;
                        continue;
                    } else {
                        tFinalRatio = tFinalRatio + finalRatio;//减掉 0.2 的 sxf
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
        List<QueryRecentSerialRedOutPutDTO> recentList = new ArrayList<>();
        Integer queryPeriod = getRateThreeIncomeInputDTO.getQueryPeriod();
        Boolean realTimeFlag = getRateThreeIncomeInputDTO.getRealTimeFlag();
        if (realTimeFlag){//如果是实时取当前时间
            queryPeriodList.add(getRateThreeIncomeInputDTO.getQueryStartTime());
        }else if (Objects.nonNull(queryPeriod)&&queryPeriod>1){
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
            List<String> shareCodeList;
            if (!realTimeFlag){//非实时
                List<QueryRecentSerialRedOutPutDTO>  outPutDTOList = marketInfoNewMapper.queryHeadRatioShareInfo(getRateThreeIncomeInputDTO);
                shareCodeList = outPutDTOList.stream().map(QueryRecentSerialRedOutPutDTO::getShareCode).collect(Collectors.toList());
            }else {//获取当天实时
                MarketInputDomain marketInputDomain = new MarketInputDomain();
                ShareInfo shareInfo = new ShareInfo();
                shareInfo.setHushenStatus("B");
                List<ShareInfo> respShareInfoList = shareInfoMapper.getShareInfo(shareInfo);
                List<String> originalShareCodeList = respShareInfoList.stream().map(shareInfo10 -> shareInfo10.getShareCode()).collect(Collectors.toList());
                marketInputDomain.setShareCodeList(originalShareCodeList);
                List<MarketRealTimeOutputDomain> realTimeOutputDomainList = sharesInfoService.getRealTimeRateByXueQiu(marketInputDomain);
                realTimeOutputDomainList = realTimeOutputDomainList.stream().filter(marketRealTimeOutputDomain -> marketRealTimeOutputDomain.getCurrentPercent()>2).collect(Collectors.toList());
                shareCodeList = realTimeOutputDomainList.stream().map(marketRealTimeOutputDomain -> marketRealTimeOutputDomain.getShareCode()).collect(Collectors.toList());
            }
            if (shareCodeList.size()<=0){
                continue;
            }
            QueryRecentSerialRedConditionDO queryRecentSerialRedConditionDO = new QueryRecentSerialRedConditionDO();
            String queryStartTime = getRateThreeIncomeInputDTO.getQueryStartTime();
            Date endTime = DateUtils.parse(queryStartTime, DateUtils.DateFormat.YYYY_MM_DD_HH_MM_SS);
            Integer recentDay = getRateThreeIncomeInputDTO.getRecentDay();
            if (realTimeFlag){
                recentDay = recentDay -1;
            }
            Date preTime = tradeInfoService.getWantDate(recentDay, endTime, "sub");
            queryRecentSerialRedConditionDO.setDownStartTime(preTime);
            queryRecentSerialRedConditionDO.setDownEndTime(endTime);
            queryRecentSerialRedConditionDO.setMinDownRate(getRateThreeIncomeInputDTO.getMinDownRate()*(-2));
            queryRecentSerialRedConditionDO.setShareCodeList(shareCodeList);
            queryRecentSerialRedConditionDO.setMinDownDay(getRateThreeIncomeInputDTO.getMinDownDay());
             recentList = marketInfoMapper.queryRecentSerialGreen(queryRecentSerialRedConditionDO);
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
        if (!realTimeFlag){
            serialTempMapper.insertList(respList,getRateThreeIncomeInputDTO.getRowNum());
            return new ArrayList<>();
        }else {
            return recentList;
        }


    }

}
