package com.liuhu.socket.service.impl;

import com.liuhu.socket.common.DateUtils;
import com.liuhu.socket.common.MathConstants;
import com.liuhu.socket.dao.*;
import com.liuhu.socket.domain.input.*;
import com.liuhu.socket.domain.output.*;
import com.liuhu.socket.dto.QueryRecentSerialRedConditionDO;
import com.liuhu.socket.dto.Xia2Shang1InnerDTO;
import com.liuhu.socket.entity.*;
import com.liuhu.socket.enums.PersonalStatusEnum;
import com.liuhu.socket.enums.TradeStatusEnum;
import com.liuhu.socket.service.TradeInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author liuhu-jk
 * @Date 2019/8/15 17:44
 * @Description
 **/
@Service
@Slf4j
public class TradeInfoServiceImpl implements TradeInfoService {
    @Resource
    TradeInfoMapper tradeInfoMapper;
    @Resource
    PersonalInfoMapper personalInfoMapper;
    @Resource
    MarketInfoMapper marketInfoMapper;
    @Resource
    PersonalDetailInfoMapper personalDetailInfoMapper;

    @Resource
    MarketInfoNewMapper marketInfoNewMapper;


    @Resource
    ShareInfoMapper shareInfoMapper;


    @Resource
    TradeDateMapper tradeDateMapper;


    @Resource
    ConditionShareInfoMapper conditionShareInfoMapper;

    @Resource
    SerialTempMapper serialTempMapper;


    public static Integer maxSockerCount = 5;




    @Override
    @Transactional
    public int operateTrade(TradeInputDomain input) {
        int returnValue = -1;
        /**
         * 校验账户信息
         */
        Map map = this.checkAccountRule(input, TradeStatusEnum.BUY.getCode());
        if (map.containsKey("back")) {
            return -1;
        }
        /**
         * 校验行情信息是否符合规范
         */
        int checkMarket = checkMarketInfo(input);
        if (checkMarket == -1) {
            return checkMarket;
        }
        TradeInfo tradeInfo = new TradeInfo();
        tradeInfo.setHandNum((double) input.getHandNum());
        tradeInfo.setPersonId(input.getPersonId());
        tradeInfo.setShareCode(input.getShareCode());
        tradeInfo.setTradeDate(DateUtils.parse(input.getInvestTime(), DateUtils.DateFormat.YYYY_MM_DD));
        tradeInfo.setTradePrice(input.getUnitValue());
        tradeInfo.setType(String.valueOf(TradeStatusEnum.BUY.getCode()));
        tradeInfo.setUpdateDate(new Date());
        int i = tradeInfoMapper.insertSelective(tradeInfo);
        PersonalInfo backPersonInfo = (PersonalInfo) map.get("personalInfo");
        if (i > 0) {
            /**
             * 购买费率
             */
            double commission = MathConstants.computerCommission(input.getUnitValue(), input.getHandNum(), TradeStatusEnum.BUY.getCode());
            double newTotalAmount = backPersonInfo.getTotalAmount();
            double newMarketAmount = backPersonInfo.getTotalMarketValue() + input.getUnitValue() * input.getHandNum();
            double newTotalShare = backPersonInfo.getTotalShare();
            backPersonInfo.setTotalAmount(newTotalAmount);
            backPersonInfo.setTotalMarketValue(newMarketAmount);
            backPersonInfo.setTotalShare(newTotalShare);
            backPersonInfo.setUpdateDate(new Date());
            backPersonInfo.setCommission(commission+backPersonInfo.getCommission());
            personalInfoMapper.updateAmountByPersonId(backPersonInfo);
            /**
             * 查询投资详情
             */
            PersonalDetailInfo detailInfo = new PersonalDetailInfo();
            detailInfo.setPersonId(input.getPersonId());
            detailInfo.setShareCode(input.getShareCode());
            detailInfo.setStatus(PersonalStatusEnum.VALID.getCode());
            List<PersonalDetailInfo> detailInfoList = personalDetailInfoMapper.queryDetailInfo(detailInfo);
            /**
             * 更新个人账户详情信息，如果有则更新  无则添加
             */
            double investAmount = input.getUnitValue() * input.getHandNum();
            if (detailInfoList != null && detailInfoList.size() > 0) {
                detailInfo = detailInfoList.get(0);
                //取购买价格为最新价格
                detailInfo.setCurrentPrice(input.getUnitValue());
                //股数为当前持有股数加上新购股数
                detailInfo.setHandNum(detailInfo.getHandNum() + input.getHandNum());
                double ownAmount = detailInfo.getHandPrice();
                //最新持有的股票总股价
                detailInfo.setHandPrice(ownAmount + investAmount);
                //持有收益为(最新股票数*最新价格）/（最新股票数*原股票价格）
                double shareAmount = detailInfo.getHandNum() * detailInfo.getCurrentPrice() - detailInfo.getHandNum() * detailInfo.getTradePrice();
                detailInfo.setShareAmount(shareAmount);
                //综合交易股价为（当前拥有的股值+新投资股值）/总股票数
                double tradePrice = (ownAmount + investAmount) / (input.getHandNum() + detailInfo.getHandNum());
                detailInfo.setTradePrice(tradePrice);
                //股票收益率为收益金额/原股价*最新股数
                double sharePer = shareAmount / (detailInfo.getHandNum() * detailInfo.getTradePrice());
                String sharePerStr = MathConstants.Pointkeep(sharePer * 100, 4) + "%";
                detailInfo.setSharePer(sharePerStr);
                detailInfo.setUpdateDate(new Date());
                detailInfo.setCommission(detailInfo.getCommission()+commission);
                returnValue = personalDetailInfoMapper.updatePersonalDetailByEntity(detailInfo);
            } else {
                detailInfo.setHandNum(input.getHandNum());
                detailInfo.setUpdateDate(new Date());
                detailInfo.setHandPrice(investAmount);
                detailInfo.setTradePrice(input.getUnitValue());
                detailInfo.setCurrentPrice(input.getUnitValue());
                detailInfo.setCommission(commission);
                returnValue = personalDetailInfoMapper.insertSelective(detailInfo);
            }
        }
        return returnValue;
    }

    @Override
    public int saleTrade(TradeInputDomain input) {
        /**
         * 校验账户信息
         */
        Map map = this.checkAccountRule(input, TradeStatusEnum.SALE.getCode());
        if (map.containsKey("back")) {
            return -1;
        }
        /**
         * 校验行情信息是否符合规范
         */
        int checkMarket = checkMarketInfo(input);
        if (checkMarket == -1) {
            return checkMarket;
        }
        /**
         * 查询投资详情
         */
        PersonalDetailInfo detailInfo = new PersonalDetailInfo();
        detailInfo.setPersonId(input.getPersonId());
        detailInfo.setShareCode(input.getShareCode());
        detailInfo.setStatus(PersonalStatusEnum.VALID.getCode());
        List<PersonalDetailInfo> detailInfoList = personalDetailInfoMapper.queryDetailInfo(detailInfo);
        if (detailInfoList == null || detailInfoList.size() <= 0) {
            log.warn("投资详情为空，不能进行赎回");
            return -1;
        }
        PersonalDetailInfo personalDetailInfo = detailInfoList.get(0);
        int ownValue = personalDetailInfo.getHandNum();
        int saleHandNum = input.getHandNum();
        if (ownValue < saleHandNum) {
            log.warn("卖出的股票数大于当前持有的股票数");
            return -1;
        }
        /**
         * 插入到交易表中
         */
        TradeInfo tradeInfo = new TradeInfo();
        tradeInfo.setHandNum((double) input.getHandNum());
        tradeInfo.setPersonId(input.getPersonId());
        tradeInfo.setShareCode(input.getShareCode());
        tradeInfo.setTradeDate(DateUtils.parse(input.getInvestTime(), DateUtils.DateFormat.YYYY_MM_DD));
        tradeInfo.setTradePrice(input.getUnitValue());
        tradeInfo.setType(String.valueOf(TradeStatusEnum.BUY.getCode()));
        tradeInfo.setStatus(PersonalStatusEnum.VALID.getCode());
        //查询持股多长时间
        List<TradeInfo> tradeList = tradeInfoMapper.queryEarlyInfo(tradeInfo);
        tradeInfo.setType(String.valueOf(TradeStatusEnum.SALE.getCode()));
        tradeInfo.setUpdateDate(new Date());
        int i = tradeInfoMapper.insertSelective(tradeInfo);
        if (i > 0) {

            double investAmount = input.getUnitValue() * input.getHandNum();
            detailInfo = detailInfoList.get(0);
            detailInfo.setCurrentPrice(input.getUnitValue());
            /**
             * 更新个股信息
             */
            if (tradeList != null && tradeList.size() > 0) {
                Date earlyDate = tradeList.get(0).getTradeDate();
                int day = DateUtils.getIntervalDaysForDay(earlyDate, DateUtils.parse(input.getInvestTime(), DateUtils.DateFormat.YYYY_MM_DD));
                detailInfo.setHoldDay(day);
            }
            //现有市值
            double ownAmount = detailInfo.getTradePrice() * detailInfo.getHandNum();
            //佣金费率
            double commission = MathConstants.computerCommission(input.getUnitValue(), input.getHandNum(), TradeStatusEnum.SALE.getCode());
            //收益金额
            double shareAmount = detailInfo.getHandNum() * input.getUnitValue() - detailInfo.getHandNum() * detailInfo.getTradePrice();
            //收益率
            double sharePer = shareAmount / ownAmount;
            String sharePerStr = MathConstants.Pointkeep(sharePer * 100, 4) + "%";
            detailInfo.setSharePer(sharePerStr);
            detailInfo.setShareAmount(shareAmount);
            if (detailInfo.getHandNum().equals(input.getHandNum())) {
                detailInfo.setHandPrice(0.0);
                detailInfo.setStatus(PersonalStatusEnum.CLEAR.getCode());
                tradeInfo.setStatus(PersonalStatusEnum.CLEAR.getCode());
                tradeInfoMapper.updateEntity(tradeInfo);
            } else {
                double handPrice = detailInfo.getHandNum() * input.getUnitValue() - investAmount;
                detailInfo.setHandPrice(handPrice);
                double tradePrice = input.getUnitValue() - shareAmount / (detailInfo.getHandNum() - input.getHandNum());
                detailInfo.setTradePrice(tradePrice);

            }
            detailInfo.setHandNum(detailInfo.getHandNum() - input.getHandNum());
            detailInfo.setUpdateDate(new Date());
            detailInfo.setCommission(detailInfo.getCommission()+commission);
            personalDetailInfoMapper.updatePersonalDetailByEntity(detailInfo);
            /**
             * 更新个人资产
             */
            PersonalInfo backPersonInfo = (PersonalInfo) map.get("personalInfo");
            double newTotalAmount = backPersonInfo.getTotalAmount() + input.getUnitValue() * input.getHandNum() - input.getHandNum() * detailInfo.getTradePrice();
            double newMarketAmount = backPersonInfo.getTotalMarketValue() - input.getUnitValue() * input.getHandNum() + shareAmount;
            double newTotalShare = backPersonInfo.getTotalShare() + shareAmount;
            backPersonInfo.setTotalAmount(newTotalAmount);
            backPersonInfo.setTotalMarketValue(newMarketAmount);
            backPersonInfo.setUpdateDate(new Date());
            backPersonInfo.setTotalShare(newTotalShare);
            backPersonInfo.setCommission(commission+backPersonInfo.getCommission());
            personalInfoMapper.updateAmountByPersonId(backPersonInfo);

        }
        return 0;
    }

    /**
     * 长期持有获取收益
     * @param input
     * @return
     */
    @Override
    public Map ownerLongIncome(MarketDetailInputDomain input) {


        if (input.getStartTimeDa()==null){
            String startTimeStr = DateUtils.operateDate(new Date(), -60, DateUtils.DateFormat.YYYY_MM_DD.getFormat());
            input.setStartTimeDa(DateUtils.getBeginOfDate(DateUtils.parse(startTimeStr,DateUtils.DateFormat.YYYY_MM_DD)));
        }

        if (input.getEndTimeDa()==null){
            input.setEndTimeDa(DateUtils.getBeginOfDate(new Date()));
        }

        if (input.getMaxCount()==null){
            input.setMaxCount(5);
        }

        if (input.getMaxUnitPrice()==null){
            input.setMaxUnitPrice(200.00);
        }

        if (input.getSumCount() == null){
            input.setSumCount(100000.00);
        }

        /**
         * 根据条件查询随机的股票代码
         */
        List<String> list = shareInfoMapper.getRandomSocketByCondition(input);
        input.setShareCodeList(list);

        List<MarketInfoNew> marketInfoNewList = marketInfoNewMapper.queryMarketInfoByParam(input);
        //根据shareCode 查询数据
        Map<String, List<MarketInfoNew>> marketInfoMap = marketInfoNewList.stream().collect(Collectors.groupingBy(MarketInfoNew::getShareCode));
        List<Map> markList = new ArrayList<>();
        for (Map.Entry entry:marketInfoMap.entrySet()){
            List<MarketInfoNew> singleList = (List<MarketInfoNew>) entry.getValue();
            singleList = singleList.stream().sorted(Comparator.comparing(MarketInfoNew::getDate)).collect(Collectors.toList());
            Double surPlus = 0.00;
            Double singleShareInfo = null;
            for (MarketInfoNew marketInfoNew:singleList){
                Map map = new HashMap();
                Double endValue = marketInfoNew.getEndValue();
                Double ratio = marketInfoNew.getRiseFallRatio();
                double floor = Math.floor((input.getSumCount() / input.getShareCodeList().size()) / (endValue * 100));
                if (singleShareInfo == null) {
                    singleShareInfo = endValue * floor * 100;
                    surPlus = input.getSumCount() / input.getShareCodeList().size() - singleShareInfo;

                } else {
                    singleShareInfo = singleShareInfo * (1 + ratio/100);
                }
                map.put("shareCode",marketInfoNew.getShareCode());
                map.put("shareName",marketInfoNew.getShareName());
                map.put("singleShareInfo",MathConstants.Pointkeep(singleShareInfo,4));
                map.put("tradeDate",marketInfoNew.getDate());
                map.put("surPlus",MathConstants.Pointkeep(surPlus,4));
                map.put("ratio",marketInfoNew.getRiseFallRatioStr());
                markList.add(map);
            }

        }
        //根据时间分组
        Map<Date, List<Map>> mapInfo = markList.stream().collect(Collectors.groupingBy(TradeInfoServiceImpl::getTradeDateByMap));
        Map<String,Object> returnMap = new TreeMap<>();
        for (Map.Entry entry:mapInfo.entrySet()){
            Map subMap = new HashMap();
            List<Map> subList = (List<Map>) entry.getValue();
            Double sumShareUnit = subList.stream().collect(Collectors.summingDouble(TradeInfoServiceImpl::getSingleShareInfoByMap));
            Double sumSurPlus = subList.stream().collect(Collectors.summingDouble(TradeInfoServiceImpl::getSurPlusByMap));
            Double sumCount = sumShareUnit +sumSurPlus;
            subMap.put("sumCount",MathConstants.Pointkeep(sumCount,4));
            subMap.put("detail",subList);
            returnMap.put(DateUtils.format((Date)entry.getKey(),DateUtils.DateFormat.YYYY_MM_DD),subMap);
        }
        return returnMap;
    }

    @Override
    public TradeDateInfo queryMaxDate() {
        return tradeDateMapper.queryMaxDate();
    }

    @Override
    public Date getWantDate(Integer recentDay,Date date,String type) {
        return tradeDateMapper.getWantDate(recentDay,date,type);
    }

    @Override
    public List<Date> queryPeriodDateList(String time, Integer period,String flag) {
        return tradeDateMapper.queryPeriodDateList(time,period,flag);
    }

    @Override
    public List<MarketOutputDomain> getPrePurchaseSocker(QueryRecentSerialRedConditionDTO input) {

        List<MarketOutputDomain> returnList = new ArrayList<>();
        Integer periodUpDay = input.getPeriodUpDay();
        for (int i =1;i<periodUpDay;i++ ){
        QueryRecentSerialRedConditionDO queryRecentSerialRedConditionDO = new QueryRecentSerialRedConditionDO();
        BeanUtils.copyProperties(input,queryRecentSerialRedConditionDO);
        queryRecentSerialRedConditionDO.setSelectStartTime(DateUtils.parse(input.getSelectStartTime(), DateUtils.DateFormat.YYYY_MM_DD_HH_MM_SS));
        queryRecentSerialRedConditionDO.setSelectEndTime(getWantDate(queryRecentSerialRedConditionDO.getRecentRateDay(), DateUtils.parse(input.getSelectStartTime(), DateUtils.DateFormat.YYYY_MM_DD_HH_MM_SS),"plus"));
        queryRecentSerialRedConditionDO.setUpStartTime(getWantDate(i,queryRecentSerialRedConditionDO.getSelectStartTime(),"sub"));
        queryRecentSerialRedConditionDO.setUpEndTime(queryRecentSerialRedConditionDO.getSelectStartTime());
        queryRecentSerialRedConditionDO.setDownEndTime(queryRecentSerialRedConditionDO.getUpStartTime());
        queryRecentSerialRedConditionDO.setDownStartTime(getWantDate(queryRecentSerialRedConditionDO.getPeriodDownDay(),queryRecentSerialRedConditionDO.getDownEndTime(),"sub"));
        List<MarketOutputDomain> marketOutputDomains = marketInfoMapper.queryPrePurchaseSocker(queryRecentSerialRedConditionDO);
            List<String> codeList = returnList.stream().map(MarketOutputDomain::getShareCode).collect(Collectors.toList());
            for (MarketOutputDomain mk:marketOutputDomains){
                if (!codeList.contains(mk.getShareCode())){
                    returnList.add(mk);
                }
        }

        }
        if (!CollectionUtils.isEmpty(returnList)){
            returnList = returnList.stream().sorted(Comparator.comparing(MarketOutputDomain::getRate).reversed()).collect(Collectors.toList());
        }
        return returnList;
    }

    @Override
    public Map<String,Object> getPreFiveAndSubFive(QueryRecentSerialRedConditionDTO input){
        List<MarketOutputDomain> list = new ArrayList<>();
        Date endDate;
        for (int i = 0;i<input.getPeriod();i++){
            endDate = getWantDate(i,DateUtils.parse(input.getEndTime(),DateUtils.DateFormat.YYYY_MM_DD_HH_MM_SS),"sub");
            QueryRecentSerialRedConditionDO queryRecentSerialRedConditionDO = new QueryRecentSerialRedConditionDO();
            BeanUtils.copyProperties(input,queryRecentSerialRedConditionDO);
            queryRecentSerialRedConditionDO.setUpStartTime(getWantDate(queryRecentSerialRedConditionDO.getPeriodUpDay(),endDate,"sub"));
            queryRecentSerialRedConditionDO.setUpEndTime(endDate);
            queryRecentSerialRedConditionDO.setDownEndTime(getWantDate(1,queryRecentSerialRedConditionDO.getUpStartTime(),"sub"));
            queryRecentSerialRedConditionDO.setDownStartTime(getWantDate(queryRecentSerialRedConditionDO.getPeriodDownDay(),queryRecentSerialRedConditionDO.getDownEndTime(),"sub"));
            List<MarketOutputDomain> marketOutputDomains = marketInfoMapper.queryPreFiveAndSubFiveSocker(queryRecentSerialRedConditionDO);
            if (!CollectionUtils.isEmpty(marketOutputDomains)){
                list.addAll(marketOutputDomains);
            }
        }
        list = list.stream().distinct().collect(Collectors.toList());
        List<MarketOutputDomain> marketOutputDomains =  marketInfoMapper.queryFiveRatioByCodeAndDate(list);
        double v = marketOutputDomains.stream().mapToDouble(marketOutputDomain -> marketOutputDomain.getRate()).average().orElse(0.0);
        Map<String,Object> resultMap = new HashMap<>();
        resultMap.put("avg",v);
        resultMap.put("count",marketOutputDomains.size());
        return resultMap;
    }

    @Override
    public Map<Date,List<String>> getFixSerialDown(QueryProfitByComProgram queryProfitByComProgram) {
        String startTime = queryProfitByComProgram.getStartTime();
        String endTime = queryProfitByComProgram.getEndTime();
        //计算开始结束的准确工作日
        TradeDateInfo startTradeDateInfo =  tradeDateMapper.queryCloserDate(startTime,"start");
        Date startDate = startTradeDateInfo.getDate();
        TradeDateInfo endTradeDateInfo = tradeDateMapper.queryCloserDate(endTime,"end");
        //计算总数量吧
        int periodDayCount = endTradeDateInfo.getId() - startTradeDateInfo.getId()+1;
        QueryFixSerialDownInDTO conditionDTO = new QueryFixSerialDownInDTO();
        Map<Date,List<String>> resultMap = queryProfitByComProgram.getResultMap();
        for (int i=0;i<periodDayCount;i++){
            Date handleDate = tradeDateMapper.getWantDate(i, startDate, "plus");
            conditionDTO.setStartDate(handleDate);

            List<QueryFixSerialDownOutDTO> fixSerialDownOutDTOList = null;
            //获取某一天满足条件的fund信息
            switch (queryProfitByComProgram.getPlan()){
                case "1"://连续down five percent
                    fixSerialDownOutDTOList = getFixSerialDownOutDTOList(conditionDTO);
                    break;
                case "2"://两连水下
                    fixSerialDownOutDTOList = getSerialDownOfRiver(conditionDTO);
                    break;
                case "3":
                    fixSerialDownOutDTOList = getFirstUpPreDown(conditionDTO);
                    break;
                    default:
                        fixSerialDownOutDTOList = getFixSerialDownOutDTOList(conditionDTO);
                        break;
            }

            if (fixSerialDownOutDTOList.size()==0){
                continue;
            }
            List<String> shareCodeList = fixSerialDownOutDTOList.stream().map(QueryFixSerialDownOutDTO::getShareCode).collect(Collectors.toList());
            //开始计算profit信息
            Date profitDate = tradeDateMapper.getWantDate(1, handleDate, "plus");
            if (Objects.isNull(resultMap)){
                resultMap = new HashMap<>();
            }
            if (Objects.isNull(resultMap.get(profitDate))){
                resultMap.put(profitDate,shareCodeList);
            }else {
                List<String> list = resultMap.get(profitDate);
                list.addAll(shareCodeList);
                resultMap.put(profitDate,list);
            }
        

        }
        for (Map.Entry entry:resultMap.entrySet()){

            Date date =(Date)entry.getKey();
            List<String> shareCodeList = (List)entry.getValue();
            List<ConditionShareCodeInfo> list = new ArrayList<>();
            for (String str:shareCodeList){
                ConditionShareCodeInfo conditionShareCodeInfo = new ConditionShareCodeInfo();
                conditionShareCodeInfo.setDate(date);
                conditionShareCodeInfo.setShareCode(str);
                conditionShareCodeInfo.setType(queryProfitByComProgram.getPlan());
                list.add(conditionShareCodeInfo);
            }
            conditionShareInfoMapper.insertList(list);
        }
        return resultMap;
    }

    private List<QueryFixSerialDownOutDTO> getFirstUpPreDown(QueryFixSerialDownInDTO conditionDTO) {
        List<QueryFixSerialDownOutDTO> respList = new ArrayList<>();
        Date startDate = conditionDTO.getStartDate();
        conditionDTO.setStartDate(getWantDate(1, startDate, "sub"));
        List<QueryFixSerialDownOutDTO> serialDownOutDTOList = serialTempMapper.queryShareInfoByCondition(conditionDTO);
        Map<String, List<QueryFixSerialDownOutDTO>> queryList = serialDownOutDTOList.stream().collect(Collectors.groupingBy(queryFixSerialDownOutDTO -> queryFixSerialDownOutDTO.getType()));
        for (Map.Entry entry:queryList.entrySet()){
            List<QueryFixSerialDownOutDTO> serialList = (List<QueryFixSerialDownOutDTO>)entry.getValue();
            QueryFixSerialDownOutDTO queryFixSerialDownOutDTO = serialList.stream().sorted(Comparator.comparing(QueryFixSerialDownOutDTO::getShareCode)).findFirst().get();
            respList.add(queryFixSerialDownOutDTO);
        }
        return respList;
    }

    @Override
    public MarketOutputDomain getProfitFromSerialDown(QueryProfitByComProgram queryProfitByComProgram) {
        MarketOutputDomain marketOutputDomain = new MarketOutputDomain();
        TradeDateInfo startTradeDateInfo =  tradeDateMapper.queryCloserDate(queryProfitByComProgram.getStartTime(),"start");
        Date startDate = startTradeDateInfo.getDate();
        TradeDateInfo endTradeDateInfo = tradeDateMapper.queryCloserDate(queryProfitByComProgram.getEndTime(),"end");
        //计算总数量吧
        int periodDayCount = endTradeDateInfo.getId() - startTradeDateInfo.getId()+1;
        //每个交易日对应的可操作数
        List<ConditionShareCodeInfo> shareCodeInfoList = conditionShareInfoMapper.queryShareCodeByCondition(queryProfitByComProgram,"1");
        Map<Date, List<ConditionShareCodeInfo>> collect = shareCodeInfoList.stream().collect(Collectors.groupingBy(conditionShareCodeInfo -> conditionShareCodeInfo.getDate()));
        Map<Date,List<ConditionShareCodeInfo>> sortMap = new TreeMap<>(collect);
        List<ConditionShareCodeInfo> needBuyList = new ArrayList<>();
        Double finalProfit = 0.0;
        //第一层遍历，遍历每个交易日
        for (int i = 0; i < periodDayCount; i++) {
            Date handleDate = tradeDateMapper.getWantDate(i, startDate, "plus");
            int holdCount = needBuyList.size();
            log.info("currentDate:{},count is:{}",handleDate,holdCount);
            //同时拥有socker数,不超过5
            if (holdCount < maxSockerCount) {
                //如果遍历的当前日期小于满足条件的日期则跳出循环
                List<ConditionShareCodeInfo> conditionList = sortMap.get(handleDate);
                if (Objects.nonNull(conditionList)&&conditionList.size() > 0) {
                    int currentShareCodeSize = conditionList.size();
                    int preBuySize = maxSockerCount - holdCount;
                    if (preBuySize > currentShareCodeSize) {
                        needBuyList.addAll(conditionList);
                    } else {
                        needBuyList.addAll(conditionList.subList(0, preBuySize));
                    }
                }
            }
            List<ConditionShareCodeInfo> tempNeedBuyList = new ArrayList(needBuyList);
            if (tempNeedBuyList.size()>0){
                for (ConditionShareCodeInfo conditionShareCodeInfo:tempNeedBuyList){
                    GetRateThreeIncomeInputDTO getRateThreeIncomeInputDTO = new GetRateThreeIncomeInputDTO();
                    getRateThreeIncomeInputDTO.setType(conditionShareCodeInfo.getShareCode());
                    getRateThreeIncomeInputDTO.setShareCode(conditionShareCodeInfo.getShareCode());
                    getRateThreeIncomeInputDTO.setQueryStartTime(DateUtils.format(conditionShareCodeInfo.getDate(),DateUtils.DateFormat.YYYY_MM_DD_HH_MM_SS));
                    getRateThreeIncomeInputDTO.setQueryEndTime(DateUtils.format(handleDate,DateUtils.DateFormat.YYYY_MM_DD_HH_MM_SS));
                    MarketRateTheeOutPutDTO outPutDTO = getRateThreeIncome(getRateThreeIncomeInputDTO);
                    Boolean isFinish = outPutDTO.getIsFinish();
                    if (isFinish){
                        needBuyList.remove(conditionShareCodeInfo);
                        finalProfit = finalProfit + outPutDTO.getAmount();
                    }
                    if (handleDate.compareTo(endTradeDateInfo.getDate())==0&&!isFinish){
                        finalProfit = finalProfit + outPutDTO.getAmount();
                    }

                }

            }
        }
        marketOutputDomain.setProfit(finalProfit);
        return marketOutputDomain;
    }

    private List<QueryFixSerialDownOutDTO> getFixSerialDownOutDTOList(QueryFixSerialDownInDTO conditionDTO) {
        List<QueryFixSerialDownOutDTO> list = new ArrayList<>();
        for (int i = 2;i<=7;i++) {
            conditionDTO.setSerialDownCount(i);
            List<QueryFixSerialDownOutDTO> queryFixSerialDownList = marketInfoMapper.queryFixSerialDown(conditionDTO);
            //根据类型分组
            Map<String, List<QueryFixSerialDownOutDTO>> typeMap = queryFixSerialDownList.stream().collect(Collectors.groupingBy(QueryFixSerialDownOutDTO::getType));
            for (Map.Entry entry : typeMap.entrySet()) {
                List<QueryFixSerialDownOutDTO> serialDownOutDTOList = (List<QueryFixSerialDownOutDTO>) entry.getValue();
                //获取分组中亏损最小的那支
                QueryFixSerialDownOutDTO queryFixSerialDownOutDTO = serialDownOutDTOList.stream().sorted(Comparator.comparing(QueryFixSerialDownOutDTO::getSumRatio)).findFirst().get();
                if (list.size() > 0) {
                    List<String> typeList = list.stream().map(QueryFixSerialDownOutDTO::getType).collect(Collectors.toList());
                    if (!typeList.contains(queryFixSerialDownOutDTO.getType())) {
                        list.add(queryFixSerialDownOutDTO);
                    }
                } else {
                    list.add(queryFixSerialDownOutDTO);
                }
            }
        }
        return list;
    }

    /**
     * 两连水下的记录
     * @param conditionDTO
     * @return
     */
    private List<QueryFixSerialDownOutDTO> getSerialDownOfRiver(QueryFixSerialDownInDTO conditionDTO){
        List<QueryFixSerialDownOutDTO> list = new ArrayList<>();
        List<QueryFixSerialDownOutDTO> queryFixSerialDownList = marketInfoMapper.getSerialDownOfRiver(conditionDTO);
        Map<String, List<QueryFixSerialDownOutDTO>> typeMap = queryFixSerialDownList.stream().collect(Collectors.groupingBy(QueryFixSerialDownOutDTO::getType));
        for (Map.Entry entry : typeMap.entrySet()) {
            List<QueryFixSerialDownOutDTO> serialDownOutDTOList = (List<QueryFixSerialDownOutDTO>) entry.getValue();
            QueryFixSerialDownOutDTO queryFixSerialDownOutDTO = serialDownOutDTOList.stream().findFirst().get();
            list.add(queryFixSerialDownOutDTO);
        }
        return list;
    }

    private static Date getTradeDateByMap(Map map){

        return (Date) map.get("tradeDate");
    }

    private static Double getSingleShareInfoByMap(Map map){

        return (Double) map.get("singleShareInfo");
    }

    private static Double getSurPlusByMap(Map map){

        return (Double) map.get("surPlus");
    }


    /**
     * 校验账户信息
     *
     * @param input
     * @return
     */
    private Map checkAccountRule(TradeInputDomain input, int type) {
        Map<String, Object> map = new HashMap<>();
        String personId = input.getPersonId();
        double investAmount = input.getUnitValue() * input.getHandNum();
        PersonalInfo personalInfo = new PersonalInfo();
        personalInfo.setPersonId(personId);
        List<PersonalInfo> list = personalInfoMapper.queryByEntity(personalInfo);
        if (list == null || list.size() <= 0) {
            log.info("查询个人账户不存在");
            map.put("back", -1);
            return map;
        }
        PersonalInfo backPersonInfo = list.get(0);
        double totalAmount = backPersonInfo.getTotalAmount();
        double totalMarket = backPersonInfo.getTotalMarketValue();
        double freeMoney = totalAmount - totalMarket;
        if (TradeStatusEnum.BUY.getCode() == type) {
            if (investAmount >= freeMoney) {
                log.info("购买金额大于可用金额");
                map.put("back", -1);
                return map;
            }
        }
        map.put("personalInfo", backPersonInfo);
        return map;
    }

    /**
     * 校验是否符合市场行情规范
     *
     * @param input
     * @return
     */
    private int checkMarketInfo(TradeInputDomain input) {
        MarketInputDomain marketInputDomain = new MarketInputDomain();
        marketInputDomain.setShareCode(input.getShareCode());
        marketInputDomain.setStartTime(input.getInvestTime());
        marketInputDomain.setEndTime(input.getInvestTime());

        List<MarketInfo> marketInfoList = marketInfoMapper.getShareInfo(marketInputDomain);
        if (marketInfoList == null || marketInfoList.size() <= 0) {
            log.info("没有当天的行情");
            return -1;
        }
        MarketInfo marketInfo = marketInfoList.get(0);
        double highest = marketInfo.getHighest();
        double lowest = marketInfo.getLowest();
        if (input.getUnitValue() < lowest || input.getUnitValue() > highest) {
            log.info("交易的价格不符合当天行情不能交易");
            return -1;
        }
        return 0;
    }

    public MarketRateTheeOutPutDTO getRateThreeIncome(GetRateThreeIncomeInputDTO getRateThreeIncomeInputDTO) {
        MarketRateTheeOutPutDTO returnRateDTO = new MarketRateTheeOutPutDTO();
        String shareCode = getRateThreeIncomeInputDTO.getShareCode();
        returnRateDTO.setMaxShareCode(shareCode);
        Integer maxDoubleSize = 1;
        getRateThreeIncomeInputDTO.setType(getRateThreeIncomeInputDTO.getShareCode());
        List<QueryRecentSerialRedOutPutDTO> minRateThreeList = serialTempMapper.getMinRateThree(getRateThreeIncomeInputDTO);
        if (minRateThreeList.size() == 0) {
            return returnRateDTO;
        }
        //最终收益率
        Double tFinalRatio = 0.0;
        //翻倍次数
        Integer doubleSize = 1;
        //实时收益率
        Double runRatio = 0.0;
        //基金净值 开始计算设置为1
        Double unitPrice = 1.0;
        //购买单元
        int unitAmount;
        Double allProfit = 0.0;

        //使用的最大倍数
        int useMaxSize = 0;

        Boolean firstFlag = true;
        for (QueryRecentSerialRedOutPutDTO queryRecentSerialRedOutPutDTO : minRateThreeList) {
            shareCode = queryRecentSerialRedOutPutDTO.getShareCode();
            Integer cycleProfit = getRateThreeIncomeInputDTO.getCycleProfit();
            //设定目标收益率不为空 且 总收益率 大于总收益率 且1个单位收益小于刚买入时跌固定目标负收益率
            if (Objects.nonNull(cycleProfit) && tFinalRatio > cycleProfit && runRatio < getRateThreeIncomeInputDTO.getRunRatio())           {
                runRatio = 0.0;
                tFinalRatio = 0.0;
                unitPrice = 1.0;
            }
            Double unitProfit = 0.0;//当天的收益
            Double finalRatio = queryRecentSerialRedOutPutDTO.getFinalRatio();
            Double maxRatio = queryRecentSerialRedOutPutDTO.getMaxRatio();
            Double minRatio = queryRecentSerialRedOutPutDTO.getMinRatio();
            Double openRatio = queryRecentSerialRedOutPutDTO.getOpenRatio();
            Double fee = getRateThreeIncomeInputDTO.getFee();
            Double fundProfit = getRateThreeIncomeInputDTO.getFundProfit();
            unitPrice = unitPrice * (1 + finalRatio * 0.01);
            //获取doubleSize
            int i = Math.abs((int) (runRatio / getRateThreeIncomeInputDTO.getDoubleSize())) + 1;
            doubleSize = i;
            if (useMaxSize<doubleSize){
                useMaxSize = doubleSize;
            }
            if (i>1){
                firstFlag = false;
            }
            unitAmount = 10000 * doubleSize;
            if (firstFlag) {
                if (openRatio + runRatio > fundProfit){
                    unitProfit = openRatio * unitAmount * 0.01 - unitAmount * fee * 0.01;
                }else if (minRatio + runRatio > fundProfit) {
                    unitProfit = minRatio * unitAmount * 0.01 - unitAmount * fee * 0.01;
                } else if (maxRatio + runRatio > fundProfit) {
                    unitProfit = (fundProfit - runRatio) * unitAmount * 0.01 - unitAmount * fee * 0.01;
                } else {
                    tFinalRatio = tFinalRatio + finalRatio;//减掉0.2 的sxf
                    runRatio = runRatio + finalRatio;
                    unitProfit =  finalRatio * unitAmount * 0.01;
                    allProfit = allProfit +unitProfit;
                    continue;
                }
                returnRateDTO.setAmount(unitProfit);
                returnRateDTO.setMaxShareCode(shareCode);
                returnRateDTO.setEndTime(queryRecentSerialRedOutPutDTO.getStartTime());
                returnRateDTO.setIsFinish(true);
             //   log.info("QueryStartTime is:{},QueryEndTime is:{},currentTime is:{},shareCode is:{},amount is:{},doubleSize:{}",getRateThreeIncomeInputDTO.getQueryStartTime(),getRateThreeIncomeInputDTO.getQueryEndTime(),queryRecentSerialRedOutPutDTO.getStartTime(),shareCode,returnRateDTO.getAmount(),doubleSize);
                return returnRateDTO;
            }

            double regularProfit= MathConstants.getPeriodProfit(getRateThreeIncomeInputDTO.getQueryStartTime(),getRateThreeIncomeInputDTO.getQueryEndTime());
            switch (doubleSize) {
                case 1:
                    break;
                case 2:
                    fee = fee * 0.6;
                    break;
                case 3:
                    fee = fee * 0.5;
                    break;
                case 4:
               //     regularProfit = regularProfit - 1;
                    fee = fee * 0.4;
                    break;
                case 5:
              //      regularProfit = regularProfit - 2;
                    fee = fee * 0.3;
                    break;
                default:
               //     regularProfit = regularProfit - 3;
                    fee = fee * 0.3;
            }

            if (openRatio*doubleSize +tFinalRatio >regularProfit*doubleSize){//如果开盘收益率*单位购买数-总亏损数 大于 常规收益
                returnRateDTO.setAmount(MathConstants.Pointkeep((openRatio*doubleSize+tFinalRatio-fee*doubleSize)*unitAmount*0.01,2));
                returnRateDTO.setMaxShareCode(shareCode);
                returnRateDTO.setEndTime(queryRecentSerialRedOutPutDTO.getStartTime());
                returnRateDTO.setIsFinish(true);
           //     log.info("QueryStartTime is:{},QueryEndTime is:{},currentTime is:{},shareCode is:{},amount is:{},doubleSize:{}",getRateThreeIncomeInputDTO.getQueryStartTime(),getRateThreeIncomeInputDTO.getQueryEndTime(),queryRecentSerialRedOutPutDTO.getStartTime(),shareCode,returnRateDTO.getAmount(),doubleSize);
                return returnRateDTO;
            } else if (maxRatio *doubleSize +tFinalRatio >regularProfit*doubleSize) {
                returnRateDTO.setAmount(MathConstants.Pointkeep((regularProfit*doubleSize -fee*doubleSize)*unitAmount*0.01,2));
                returnRateDTO.setMaxShareCode(shareCode);
                returnRateDTO.setEndTime(queryRecentSerialRedOutPutDTO.getStartTime());
                returnRateDTO.setIsFinish(true);
            //    log.info("QueryStartTime is:{},QueryEndTime is:{},currentTime is:{},shareCode is:{},amount is:{},doubleSize:{}",getRateThreeIncomeInputDTO.getQueryStartTime(),getRateThreeIncomeInputDTO.getQueryEndTime(),queryRecentSerialRedOutPutDTO.getStartTime(),shareCode,returnRateDTO.getAmount(),doubleSize);
                return returnRateDTO;
            } else {
                tFinalRatio = tFinalRatio + finalRatio * doubleSize;
                runRatio = runRatio + finalRatio;
                unitProfit = finalRatio * unitAmount * 0.01;
                allProfit = allProfit + unitProfit;
                continue;
            }

        }

        returnRateDTO.setAmount(tFinalRatio*100);
        returnRateDTO.setMaxDoubleSize(maxDoubleSize);
        returnRateDTO.setIsFinish(false);
        return returnRateDTO;
    }


}
