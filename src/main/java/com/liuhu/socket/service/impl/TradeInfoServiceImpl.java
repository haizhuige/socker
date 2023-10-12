package com.liuhu.socket.service.impl;

import com.liuhu.socket.common.ConstantsUtil;
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
import com.liuhu.socket.service.SharesInfoService;
import com.liuhu.socket.service.TradeInfoService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
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


    @Autowired
    SharesInfoService sharesInfoService;


    public static Integer maxSockerCount = 5;

    @Qualifier("taskExecutor")
    @Autowired
    ThreadPoolTaskExecutor taskExecutor;


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
        for (int i = 1; i < periodUpDay; i++) {
            QueryRecentSerialRedConditionDO queryRecentSerialRedConditionDO = new QueryRecentSerialRedConditionDO();
            BeanUtils.copyProperties(input, queryRecentSerialRedConditionDO);
            queryRecentSerialRedConditionDO.setSelectStartTime(DateUtils.parse(input.getSelectStartTime(), DateUtils.DateFormat.YYYY_MM_DD_HH_MM_SS));
            queryRecentSerialRedConditionDO.setSelectEndTime(getWantDate(queryRecentSerialRedConditionDO.getRecentRateDay(), DateUtils.parse(input.getSelectStartTime(), DateUtils.DateFormat.YYYY_MM_DD_HH_MM_SS), "plus"));
            queryRecentSerialRedConditionDO.setUpStartTime(getWantDate(i, queryRecentSerialRedConditionDO.getSelectStartTime(), "sub"));
            queryRecentSerialRedConditionDO.setUpEndTime(queryRecentSerialRedConditionDO.getSelectStartTime());
            queryRecentSerialRedConditionDO.setDownEndTime(queryRecentSerialRedConditionDO.getUpStartTime());
            queryRecentSerialRedConditionDO.setDownStartTime(getWantDate(queryRecentSerialRedConditionDO.getPeriodDownDay(), queryRecentSerialRedConditionDO.getDownEndTime(), "sub"));
            List<MarketOutputDomain> marketOutputDomains = marketInfoMapper.queryPrePurchaseSocker(queryRecentSerialRedConditionDO);
            List<String> codeList = returnList.stream().map(MarketOutputDomain::getShareCode).collect(Collectors.toList());
            for (MarketOutputDomain mk : marketOutputDomains) {
                if (!codeList.contains(mk.getShareCode())) {
                    returnList.add(mk);
                }
            }

        }
        if (!CollectionUtils.isEmpty(returnList)) {
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
        if (Objects.isNull(startTime)){
            startTime =  conditionShareInfoMapper.queryMaxStartTime();
            if (Objects.isNull(startTime)){
                startTime = "2018-01-03 00:00:00";
            }
        }
        String endTime = queryProfitByComProgram.getEndTime();
        if (Objects.isNull(endTime)){
            TradeDateInfo tradeDateInfo = tradeDateMapper.queryMaxDate();
            endTime = DateUtils.format(tradeDateInfo.getDate(),DateUtils.DateFormat.YYYY_MM_DD_HH_MM_SS);
        }
        List<String> planList;
        //计算开始结束的准确工作日
        TradeDateInfo startTradeDateInfo =  tradeDateMapper.queryCloserDate(startTime,"start");
        Date startDate = startTradeDateInfo.getDate();
        TradeDateInfo endTradeDateInfo = tradeDateMapper.queryCloserDate(endTime,"end");
        //计算总数量吧
        int periodDayCount = endTradeDateInfo.getId() - startTradeDateInfo.getId()+1;
        QueryFixSerialDownInDTO conditionDTO = new QueryFixSerialDownInDTO();
        final  Map<Date,List<String>> resultMap = new HashMap<>();
        List<QueryFixSerialDownOutDTO> allShareInfoList = new ArrayList<>();
        for ( int i=0;i<periodDayCount;i++) {
            final int temp = i;
            Date handleDate = tradeDateMapper.getWantDate(temp, startDate, "plus");
            conditionDTO.setStartDate(handleDate);
            planList= queryProfitByComProgram.getPlanList();
            if (Objects.isNull(planList)){
                planList = Arrays.asList("1","2");
            }else if (planList.size()==0&&Objects.nonNull(queryProfitByComProgram.getPlan())){
                planList.add(queryProfitByComProgram.getPlan());
            }
            List<QueryFixSerialDownOutDTO> unitAllFixSerialDownOutList = new ArrayList<>();
            Date profitDate;
            for (String plan:planList){
                //获取某一天满足条件的fund信息
                List<QueryFixSerialDownOutDTO> fixSerialDownOutDTOList;
                switch (plan) {
                    case "1"://连续down five percent
                        fixSerialDownOutDTOList = getFixSerialDownOutDTOList(conditionDTO);
                        profitDate = handleDate;
                        break;
                    case "2"://两连水下
                        fixSerialDownOutDTOList = getSerialDownOfRiver(conditionDTO);
                        profitDate = handleDate;
                        break;
                    case "3":
                        fixSerialDownOutDTOList = getFirstUpPreDown(conditionDTO);
                        profitDate = tradeDateMapper.getWantDate(1, handleDate, "sub");
                        break;
                    default:
                        fixSerialDownOutDTOList = getFixSerialDownOutDTOList(conditionDTO);
                        profitDate = handleDate;
                        break;
                }

                if (fixSerialDownOutDTOList.size() == 0) {
                    continue;
                }
                //过滤出所有满足条件的shareCode
                List<String> shareCodeList = fixSerialDownOutDTOList.stream().map(QueryFixSerialDownOutDTO::getShareCode).collect(Collectors.toList());
                //开始计算profit信息
                if (Objects.isNull(resultMap.get(profitDate))) {
                    resultMap.put(profitDate, shareCodeList);
                } else {
                    List<String> list = resultMap.get(profitDate);
                    list.addAll(shareCodeList);
                    resultMap.put(profitDate, list);
                }
                //对满足条件的设置购买时间
                Date finalProfitDate = profitDate;
                fixSerialDownOutDTOList.forEach(entity -> {entity.setHandleDate(finalProfitDate);
                entity.setPlan(plan);});
                //把每次方案过滤出来满足条件的数据新增到单个代码中
                unitAllFixSerialDownOutList.addAll(fixSerialDownOutDTOList);
            }
            //如果当前日期下没有满足条件的代码,则进入下一轮循环
            if (unitAllFixSerialDownOutList.size()==0){
                continue;
            }
            //把满足条件的个股加入到所有满足条件的list中
            allShareInfoList.addAll(unitAllFixSerialDownOutList);
        }
        try {
            List<ConditionShareCodeInfo> list = new ArrayList<>();
            for (QueryFixSerialDownOutDTO outDTO:allShareInfoList){
                ConditionShareCodeInfo conditionShareCodeInfo = new ConditionShareCodeInfo();
                conditionShareCodeInfo.setDate(outDTO.getHandleDate());
                conditionShareCodeInfo.setShareCode(outDTO.getShareCode());
                conditionShareCodeInfo.setType(outDTO.getPlan());
                conditionShareCodeInfo.setCountDay(outDTO.getCountDay());
                conditionShareCodeInfo.setSumRatio(outDTO.getSumRatio());
                list.add(conditionShareCodeInfo);
            }
            if (list.size()>0){
                conditionShareInfoMapper.insertList(list);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultMap;
    }

    private List<QueryFixSerialDownOutDTO> getFirstUpPreDown(QueryFixSerialDownInDTO conditionDTO) {
        List<QueryFixSerialDownOutDTO> respList = new ArrayList<>();
        Date startDate = conditionDTO.getStartDate();
        conditionDTO.setStartDate(startDate);
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
        Integer maxRegularCount = queryProfitByComProgram.getMaxValue();
        if (Objects.isNull(maxRegularCount)){
            maxRegularCount = maxSockerCount;
        }
        TradeDateInfo startTradeDateInfo = tradeDateMapper.queryCloserDate(queryProfitByComProgram.getStartTime(), "start");
        Date startDate = startTradeDateInfo.getDate();
        TradeDateInfo endTradeDateInfo = tradeDateMapper.queryCloserDate(queryProfitByComProgram.getEndTime(), "end");
        //计算总数量吧
        int periodDayCount = endTradeDateInfo.getId() - startTradeDateInfo.getId() + 1;
        //每个交易日对应的可操作数
        List<ConditionShareCodeInfo> shareCodeInfoList = conditionShareInfoMapper.queryShareCodeByCondition(queryProfitByComProgram);
        //按照日期进行分组
        Map<Date, List<ConditionShareCodeInfo>> groupByDatePreSelectionCodeMap = shareCodeInfoList.stream().collect(Collectors.groupingBy(conditionShareCodeInfo -> conditionShareCodeInfo.getDate()));
        //按照日期排序
        Map<Date, List<ConditionShareCodeInfo>> preSelectionSortMap = new TreeMap<>(groupByDatePreSelectionCodeMap);
        //需要购买的集合
        List<ConditionShareCodeInfo> needBuyList = new ArrayList<>();
        Double finalProfit = 0.0;
        Map<Date, List<DetailInfoBuyTypeBuyDTO>> detailMap = new TreeMap<>();
        Map<String,Integer> countMap = new HashMap<>();
        //第一层遍历，遍历每个交易日
        Integer amount = 0;
        for (int i = 0; i < periodDayCount; i++) {
            Date handleDate = tradeDateMapper.getWantDate(i, startDate, "plus");
            int holdCount = needBuyList.size();
            //同时拥有socker数,不超过5
            if (holdCount < maxRegularCount) {
                List<ConditionShareCodeInfo> conditionList = preSelectionSortMap.get(handleDate);
                if (Objects.nonNull(conditionList) && conditionList.size() > 0) {
                    conditionList =  conditionList.stream().filter(conditionShareCodeInfo -> conditionShareCodeInfo.getSumRatio()<-0.4).collect(Collectors.toList());
                    conditionList = sortConditionList(conditionList);
                    int currentShareCodeSize = conditionList.size();
                    int preBuySize = maxRegularCount - holdCount;
                    if (preBuySize > currentShareCodeSize) {
                        needBuyList.addAll(conditionList);
                        amount = amount + conditionList.size();
                    } else {
                        needBuyList.addAll(conditionList.subList(0, preBuySize));
                        amount = amount + preBuySize;
                    }
                }
            }
            List<ConditionShareCodeInfo> tempNeedBuyList = new ArrayList(needBuyList);
            List<DetailInfoBuyTypeBuyDTO> subDetailList = new ArrayList();
            if (tempNeedBuyList.size() > 0) {
                for (ConditionShareCodeInfo conditionShareCodeInfo : tempNeedBuyList) {
                    String shareCode = conditionShareCodeInfo.getShareCode();
                    DetailInfoBuyTypeBuyDTO detailInfoBuyTypeBuyDTO = new DetailInfoBuyTypeBuyDTO();
                    GetRateThreeIncomeInputDTO getRateThreeIncomeInputDTO = new GetRateThreeIncomeInputDTO();
                    getRateThreeIncomeInputDTO.setType(shareCode);
                    getRateThreeIncomeInputDTO.setShareCode(shareCode);
                    getRateThreeIncomeInputDTO.setQueryStartTime(DateUtils.format(handleDate, DateUtils.DateFormat.YYYY_MM_DD_HH_MM_SS));
                    getRateThreeIncomeInputDTO.setQueryEndTime(DateUtils.format(handleDate, DateUtils.DateFormat.YYYY_MM_DD_HH_MM_SS));
                    getRateThreeIncomeInputDTO.setAllProfit(conditionShareCodeInfo.getAllProfit());
                    getRateThreeIncomeInputDTO.setTFinalRatio(conditionShareCodeInfo.getTFinalRatio());
                    getRateThreeIncomeInputDTO.setMethodRunRatio(conditionShareCodeInfo.getMethodRunRatio());
                    getRateThreeIncomeInputDTO.setK(conditionShareCodeInfo.getK());
                    getRateThreeIncomeInputDTO.setStartDate(conditionShareCodeInfo.getStartDate());
                    MarketRateTheeOutPutDTO outPutDTO = getRateThreeIncome(getRateThreeIncomeInputDTO);
                    needBuyList.remove(conditionShareCodeInfo);
                    conditionShareCodeInfo.setIsFinish(outPutDTO.getIsFinish());
                    conditionShareCodeInfo.setEndDate(outPutDTO.getEndTime());
                    conditionShareCodeInfo.setMethodRunRatio(outPutDTO.getMethodRunRatio());
                    conditionShareCodeInfo.setTFinalRatio(outPutDTO.getTFinalRatio());
                    conditionShareCodeInfo.setAllProfit(outPutDTO.getAllProfit());
                    conditionShareCodeInfo.setUnitProfit(outPutDTO.getUnitProfit());
                    conditionShareCodeInfo.setDoubleSize(outPutDTO.getMaxDoubleSize());
                    conditionShareCodeInfo.setK(outPutDTO.getK());
                    conditionShareCodeInfo.setStartDate(outPutDTO.getStartTime());
                    needBuyList.add(conditionShareCodeInfo);
                    BeanUtils.copyProperties(conditionShareCodeInfo,detailInfoBuyTypeBuyDTO);
                    subDetailList.add(detailInfoBuyTypeBuyDTO);
                    Boolean isFinish = outPutDTO.getIsFinish();
                    if (isFinish) {
                        needBuyList.remove(conditionShareCodeInfo);
                        finalProfit = finalProfit + outPutDTO.getAmount();
                        Integer countDay = tradeDateMapper.queryCountDay(outPutDTO.getEndTime(), outPutDTO.getStartTime());
                        countMap.put(shareCode+"|"+DateUtils.format(outPutDTO.getStartTime(),DateUtils.DateFormat.YYYY_MM_DD_HH_MM_SS),countDay);
                    }
                    if (handleDate.compareTo(endTradeDateInfo.getDate()) == 0 && !isFinish) {
                        finalProfit = finalProfit + outPutDTO.getAmount();
                    }

                }
                detailMap.put(handleDate, subDetailList);

            }

        }

      Double sumCount= 0.0;
        for (Map.Entry<Date,List<DetailInfoBuyTypeBuyDTO>> entry : detailMap.entrySet()) {
            Date key = entry.getKey();
            List<DetailInfoBuyTypeBuyDTO> value = entry.getValue();
            log.info("当前日期下：{}", key);
            for (DetailInfoBuyTypeBuyDTO conditionShareCodeInfo : value) {
                String subShareCode = conditionShareCodeInfo.getShareCode();
                Double unitProfit = conditionShareCodeInfo.getUnitProfit();
                sumCount = sumCount +unitProfit;
                Integer subDoubleSize = conditionShareCodeInfo.getDoubleSize();
                log.info("shareCode:{},unitProfit:{},倍数为:{}", subShareCode, unitProfit,subDoubleSize);
            }

        }

        Collection<Integer> values = countMap.values();
        log.info("当前总数量为：{},已回收的总数量为:{}",amount,values.size());
        Map<Integer, List<Integer>> collect = values.stream().collect(Collectors.groupingBy(Integer::intValue));
        //多少个工作日满足条件详情
        for (Map.Entry<String,Integer> entry:countMap.entrySet()){
             log.info("当前shareCode:{},countDay:{}",entry.getKey(),entry.getValue());
        }
        //多少个工作日满足条件卖出汇总
        for (Map.Entry<Integer,List<Integer>> entry:collect.entrySet()){
            List<Integer> value = entry.getValue();
            log.info("当前回收天数:{},有{}次",entry.getKey(),value.size());
        }
        marketOutputDomain.setProfit(finalProfit);
        return marketOutputDomain;
    }


    public List<ConditionShareCodeInfo> sortConditionList(List<ConditionShareCodeInfo> originalList){
        if (Objects.isNull(originalList)){
            return new ArrayList<>();
        }
        for (ConditionShareCodeInfo conditionShareCodeInfo:originalList){
             Double tempScore = 0.0;
            Integer countDay = conditionShareCodeInfo.getCountDay();
            String type = conditionShareCodeInfo.getType();
            Double sumRatio = conditionShareCodeInfo.getSumRatio();
            if ("2".equals(type)){
                tempScore = (countDay-2)*3.0;
                tempScore = tempScore -Math.abs(sumRatio+1)*0.25;
            }else if ("1".equals(type)){
                tempScore = (countDay-3)*1.0;
                tempScore = tempScore -Math.abs(sumRatio+5)*0.25;
            }
            Double initScore = conditionShareCodeInfo.getScore();
            if (Objects.isNull(initScore)){
                conditionShareCodeInfo.setScore(tempScore);
            }else {
                conditionShareCodeInfo.setScore((tempScore+initScore)/2+5.0);
            }
        }
        //去重
        List<ConditionShareCodeInfo> shareCodeInfoList = groupByFirstRecord(originalList);
        //根据分数进行排序
        Collections.sort(shareCodeInfoList,Comparator.comparingDouble(ConditionShareCodeInfo::getScore).reversed());
        return shareCodeInfoList;
    }

    @Override
    public ConditionShareCodeInfo getAllResultByConditionRule(GetRateThreeIncomeInputDTO conditionDTO) {
        ConditionShareCodeInfo conditionShareCodeInfo = new ConditionShareCodeInfo();
        //查询出所有满足规则的第二天的行情数据
        List<QueryRecentSerialRedOutPutDTO> allRuleConditionResult = serialTempMapper.getAllRuleConditionResult(conditionDTO);
        for (QueryRecentSerialRedOutPutDTO outPutDTO:allRuleConditionResult){
            List<MarketInfoNew> allRecentDayList = serialTempMapper.getAllRecentDay(outPutDTO);
            Double openValue = allRecentDayList.get(0).getOpenValue();
            Double openRatio = outPutDTO.getOpenRatio();
            Double preValue = new BigDecimal(openValue/(1+openRatio*0.01)).setScale(3,BigDecimal.ROUND_HALF_UP).doubleValue();
            Double saleValue = new BigDecimal(preValue*1.004).setScale(3,BigDecimal.ROUND_HALF_UP).doubleValue();
            Double realSaleValue;
            for (MarketInfoNew marketInfoNew:allRecentDayList){
                Double highest = marketInfoNew.getHighest();
                Double everyOpenValue = marketInfoNew.getOpenValue();
                if (everyOpenValue>=saleValue){
                    realSaleValue = everyOpenValue;
                    outPutDTO.setRealSaleRatio(new BigDecimal((realSaleValue-preValue)/preValue*100).setScale(3,BigDecimal.ROUND_HALF_UP).doubleValue());
                    outPutDTO.setEndTime(marketInfoNew.getDate());
                    break;
                }else if (highest>=saleValue){
                    realSaleValue = saleValue;
                    outPutDTO.setRealSaleRatio(new BigDecimal((realSaleValue-preValue)/preValue*100).setScale(3,BigDecimal.ROUND_HALF_UP).doubleValue());
                    outPutDTO.setEndTime(marketInfoNew.getDate());
                    break;
                }
            }
            MarketInfoNew lastMarketInfo = allRecentDayList.stream()
                    .reduce((first, second) -> second)
                    .orElse(null);
            if (Objects.isNull(outPutDTO.getEndTime())){
                outPutDTO.setRealSaleRatio(new BigDecimal((lastMarketInfo.getEndValue()-preValue)/preValue*100).setScale(3,BigDecimal.ROUND_HALF_UP).doubleValue());
                outPutDTO.setEndTime(lastMarketInfo.getDate());
            }

        }
        Map<Date, List<QueryRecentSerialRedOutPutDTO>> collect = allRuleConditionResult.stream().collect(Collectors.groupingBy(o -> o.getStartTime()));
        Double amount = 300000D;
        List<QueryRecentSerialRedOutPutDTO> holdList = new ArrayList<>();
        Double allProfit = 0.0;
        for (Map.Entry<Date,List<QueryRecentSerialRedOutPutDTO>> entry:collect.entrySet()) {
            List<QueryRecentSerialRedOutPutDTO> value = entry.getValue();
            Date currentDate = entry.getKey();
            int holdSize = holdList.size();
            int preSelectionSize = value.size();
            Double sum = 0.0;
            if (holdList.size() > 0) {
                sum = holdList.size() * amount / 5;
            }
            Double canBuyHold = amount - sum;
            //如果持有数量小于5的情况下操作
            if (holdSize < 5 && canBuyHold > 0) {
                int needCount = maxSockerCount - holdSize;
                //如果可选择的数大于可以购买的数量
                if (preSelectionSize >= needCount) {
                    List<QueryRecentSerialRedOutPutDTO> outPutDTOList = value.subList(0, needCount);
                    holdList.addAll(outPutDTOList);
                }
                List<QueryRecentSerialRedOutPutDTO> reduceList = holdList.stream().filter(queryRecentSerialRedOutPutDTO -> currentDate.compareTo(queryRecentSerialRedOutPutDTO.getEndTime()) == 0).collect(Collectors.toList());
                for (QueryRecentSerialRedOutPutDTO queryRecentSerialRedOutPutDTO : reduceList) {
                    Double realSaleRatio = queryRecentSerialRedOutPutDTO.getRealSaleRatio();
                    Double profit = new BigDecimal(amount / 5 * realSaleRatio*0.01).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue();
                    allProfit = allProfit + profit;
                }
                holdList.removeAll(reduceList);
            }
        }
     /*
       List<QueryRecentSerialRedOutPutDTO> recentSerialRedOutPutList = allRuleConditionResult.stream().filter(queryRecentSerialRedOutPutDTO -> queryRecentSerialRedOutPutDTO.getMaxRatio() < 0.4).collect(Collectors.toList());
        int noSatisfyCount = recentSerialRedOutPutList.size();
      int allCount = allRuleConditionResult.size();
      log.info("不满足的条数为:{}",noSatisfyCount);
        allRuleConditionResult.removeAll(recentSerialRedOutPutList);
        Double allProfit = 0.0;
       for (QueryRecentSerialRedOutPutDTO outPutDTO:allRuleConditionResult){
             if (outPutDTO.getOpenRatio().compareTo(0.4)>0){
                 allProfit = allProfit + outPutDTO.getOpenRatio();
             }else {
                 allProfit = allProfit + 0.4;
             }
        }*/
     /*   for (QueryRecentSerialRedOutPutDTO outPutDTO:recentSerialRedOutPutList){
            MarketInfoNew noSatisfyRecentDay = serialTempMapper.getNoSatisfyRecentDay(outPutDTO);
            Double highest = noSatisfyRecentDay.getHighest();
            Double openValue = noSatisfyRecentDay.getOpenValue();
            Double openRatio = outPutDTO.getOpenRatio();
            Double endValue = noSatisfyRecentDay.getEndValue();
            BigDecimal startValue = new BigDecimal(openValue/(1+openRatio*0.01)).setScale(3, BigDecimal.ROUND_HALF_UP);
            Double preValue = startValue.doubleValue();
            Double periodMaxRatio = new BigDecimal((highest - preValue)/preValue*100).doubleValue();
            if (periodMaxRatio>0.4){
                noSatisfyCount--;
                allProfit = allProfit +0.4;
            }else {
                Double finalRatio = new BigDecimal((endValue - preValue) / preValue * 100).doubleValue();
                allProfit = allProfit + finalRatio;
            }
        }
        conditionShareCodeInfo.setCountDay(allCount);
        conditionShareCodeInfo.setK(noSatisfyCount);
        conditionShareCodeInfo.setSumRatio(allProfit);*/
        conditionShareCodeInfo.setAllProfit(allProfit);
        return conditionShareCodeInfo;
    }

    /**
     * 根据不同方式选择标的，如果重复了,以分数高的为准
     * @param records
     * @return
     */
    public static List<ConditionShareCodeInfo> groupByFirstRecord(List<ConditionShareCodeInfo> records) {
        Map<String, ConditionShareCodeInfo> typeRecords = new HashMap<>();

        for (ConditionShareCodeInfo record : records) {
            String type = record.getShareCode();
            ConditionShareCodeInfo conditionShareCodeInfo = typeRecords.get(type);
            if (Objects.isNull(conditionShareCodeInfo)){
                typeRecords.put(type,record);
            }else {
                Double initScore = conditionShareCodeInfo.getScore();
                Double recentScore = record.getScore();
                if (recentScore.compareTo(initScore)>0){
                    typeRecords.put(type,record);
                }
            }
        }

        return new ArrayList<>(typeRecords.values());
    }

    private List<QueryFixSerialDownOutDTO> getFixSerialDownOutDTOList(QueryFixSerialDownInDTO conditionDTO) {
        List<QueryFixSerialDownOutDTO> list = new ArrayList<>();
        List<QueryFixSerialDownOutDTO> queryFixSerialDownList = marketInfoMapper.queryFixSerialDownOptimize(conditionDTO);
        Map<String, List<QueryFixSerialDownOutDTO>> typeMap = queryFixSerialDownList.stream().collect(Collectors.groupingBy(QueryFixSerialDownOutDTO::getType));
        for (Map.Entry entry : typeMap.entrySet()) {
            List<QueryFixSerialDownOutDTO> serialDownOutDTOList = (List<QueryFixSerialDownOutDTO>) entry.getValue();
            //获取分组中亏损最小的那支
            QueryFixSerialDownOutDTO queryFixSerialDownOutDTO = serialDownOutDTOList.stream().sorted(Comparator.comparing(QueryFixSerialDownOutDTO::getSumRatio).reversed()).findFirst().get();
            List<String> typeList = list.stream().map(QueryFixSerialDownOutDTO::getType).collect(Collectors.toList());
            if (!typeList.contains(queryFixSerialDownOutDTO.getType())) {
                list.add(queryFixSerialDownOutDTO);
            }
        }
        return list;
    }


    public List<QueryFixSerialDownOutDTO> getPreSelectionSerialDownDTOList(QueryFixSerialDownInDTO conditionDTO) {
        List<QueryFixSerialDownOutDTO> list = new ArrayList<>();
        QueryFixSerialDownInDTO tempCondition = new QueryFixSerialDownInDTO();
        BeanUtils.copyProperties(conditionDTO,tempCondition);
        tempCondition.setStartDate(DateUtils.parse(conditionDTO.getStartDateStr(), DateUtils.DateFormat.YYYY_MM_DD_HH_MM_SS));
        tempCondition.setMinSerialCount(2);
        tempCondition.setFixDownRatio(0.0);
        List<QueryFixSerialDownOutDTO> queryFixSerialDownList = marketInfoMapper.queryFixSerialDownOptimize(tempCondition);
        List<QueryFixSerialDownOutDTO> tempList = new ArrayList<>(queryFixSerialDownList);
        for (QueryFixSerialDownOutDTO queryDTO : tempList) {
            MarketInputDomain marketInputDomain = new MarketInputDomain();
            marketInputDomain.setShareCode(queryDTO.getShareCode());
            List<MarketRealTimeOutputDomain> realTimeInfoList = sharesInfoService.getRealTimeRateByXueQiu(marketInputDomain);
            MarketRealTimeOutputDomain marketRealTimeOutputDomain = realTimeInfoList.get(0);
            Double currentPercent = marketRealTimeOutputDomain.getCurrentPercent();
            if (currentPercent < 0) {
                queryDTO.setSumRatio(queryDTO.getSumRatio() + currentPercent);
            } else {
                queryFixSerialDownList.remove(queryDTO);
            }
        }
        log.info("----------------");
        queryFixSerialDownList = queryFixSerialDownList.stream().filter(queryFixSerialDownOutDTO -> queryFixSerialDownOutDTO.getSumRatio().compareTo(conditionDTO.getFixDownRatio()) <= 0).collect(Collectors.toList());
        //根据类型分组
        Map<String, List<QueryFixSerialDownOutDTO>> typeMap = queryFixSerialDownList.stream().collect(Collectors.groupingBy(QueryFixSerialDownOutDTO::getType));
        for (Map.Entry entry : typeMap.entrySet()) {
            List<QueryFixSerialDownOutDTO> serialDownOutDTOList = (List<QueryFixSerialDownOutDTO>) entry.getValue();
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
        return list;
    }

    /**
     * 两连水下的记录
     * @param conditionDTO
     * @return
     */
    private List<QueryFixSerialDownOutDTO> getSerialDownOfRiver(QueryFixSerialDownInDTO conditionDTO){
        List<QueryFixSerialDownOutDTO> list = new ArrayList<>();
        conditionDTO.setMinDownRiver(2);
        conditionDTO.setMinSumRatio(-1.0);
        List<QueryFixSerialDownOutDTO> queryFixSerialDownList = marketInfoMapper.getPreSelectionSerialDownOfRiver(conditionDTO);
        //根据类型分组,获取分组最小
        Map<String, List<QueryFixSerialDownOutDTO>> typeMap = queryFixSerialDownList.stream().collect(Collectors.groupingBy(QueryFixSerialDownOutDTO::getType));
        for (Map.Entry entry : typeMap.entrySet()) {
            List<QueryFixSerialDownOutDTO> serialDownOutDTOList = (List<QueryFixSerialDownOutDTO>) entry.getValue();
            //倒排序获取第一条
            QueryFixSerialDownOutDTO queryFixSerialDownOutDTO =  serialDownOutDTOList.stream().sorted(Comparator.comparing(query -> -1*query.getSumRatio())).findFirst().get();
            list.add(queryFixSerialDownOutDTO);
        }
        return list;
    }


    /**
     * 两连水下的记录
     * @param conditionDTO
     * @return
     */
    public List<QueryFixSerialDownOutDTO> preSelectionGetSerialDownOfRiver(QueryFixSerialDownInDTO conditionDTO){
        List<QueryFixSerialDownOutDTO> list = new ArrayList<>();
        conditionDTO.setStartDate(DateUtils.parse(conditionDTO.getStartDateStr(),DateUtils.DateFormat.YYYY_MM_DD_HH_MM_SS));
        conditionDTO.setMinDownRiver(1);
        conditionDTO.setMinSumRatio(-0.5);
        List<QueryFixSerialDownOutDTO> queryFixSerialDownList = marketInfoMapper.getPreSelectionSerialDownOfRiver(conditionDTO);
         List<QueryFixSerialDownOutDTO>  tempList = new ArrayList<>(queryFixSerialDownList);
        for (QueryFixSerialDownOutDTO outDTO:tempList){
            MarketInputDomain marketInputDomain = new MarketInputDomain();
            marketInputDomain.setShareCode(outDTO.getShareCode());
            List<MarketRealTimeOutputDomain> realTimeInfoList = sharesInfoService.getRealTimeRateByXueQiu(marketInputDomain);
            MarketRealTimeOutputDomain marketRealTimeOutputDomain = realTimeInfoList.get(0);
            Double high = marketRealTimeOutputDomain.getHigh();
            if (high> marketRealTimeOutputDomain.getLastClose()){
                queryFixSerialDownList.remove(outDTO);
            }
        }
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

    /**
     *  计算当前工作日需要操作的socket价格
     */
    public CalNextSaleOrBuyOutDTO getNextHandleSharePrice(CalNextSaleOrBuyInputDTO calNextSaleOrBuyInputDTO) {
        CalNextSaleOrBuyOutDTO result = new CalNextSaleOrBuyOutDTO();
        String shareCode = calNextSaleOrBuyInputDTO.getShareCode();
        result.setShareCode(calNextSaleOrBuyInputDTO.getShareCode());
        String currentDate = DateUtils.format(DateUtils.getBeginOfDate(new Date()), DateUtils.DateFormat.YYYY_MM_DD_HH_MM_SS);
        String startDate = calNextSaleOrBuyInputDTO.getStartTime();
        Double costPrice = calNextSaleOrBuyInputDTO.getCostPrice();
        Integer holdCount = calNextSaleOrBuyInputDTO.getHoldCount();
        Double currentFinalRatio = calNextSaleOrBuyInputDTO.getCurrentFinalRatio();
        Boolean firstFlag = false;
        if (currentDate.equals(startDate)) {
            firstFlag = true;
        }
        int currentHour = DateUtils.getCurrentHour();
        if (firstFlag) {//current day buy
            result.setPreHandlePrice(MathConstants.Pointkeep(costPrice * 1.004, 3));
            result.setPreHandleCount(holdCount);
            if (currentHour>ConstantsUtil.earlyHandleTime&&getCurrentMarketInfo(shareCode).getHigh()<ConstantsUtil.initSaleRatio&&currentFinalRatio<-1*ConstantsUtil.unitHandleRatio){
                double buyDoubleSize = Math.abs(Math.floor(currentFinalRatio/ConstantsUtil.unitHandleRatio));
                result.setPreHandleCount((int)buyDoubleSize * holdCount);
                result.setFinalHandleType("b");
            }
            return result;
        } else {
            Double regularProfit= MathConstants.getPeriodProfit(startDate,currentDate);
            Integer currentDoubleSize = calNextSaleOrBuyInputDTO.getCurrentDoubleSize();
            Double tFinalRatio = calNextSaleOrBuyInputDTO.getTFinalRatio();
            Double preSaleRatio = MathConstants.Pointkeep((regularProfit * currentDoubleSize-tFinalRatio)/currentDoubleSize,2);
            Double preSalePrice = MathConstants.Pointkeep(costPrice*(1+preSaleRatio),2);
            result.setPreHandleCount(holdCount);
            result.setPreHandlePrice(preSalePrice);
            if (currentHour>ConstantsUtil.earlyHandleTime&&getCurrentMarketInfo(shareCode).getHigh()<preSaleRatio){//当前最大收益率小于目标sale 收益率
                //单位收益率
                Double unitRatio = MathConstants.Pointkeep(tFinalRatio / currentDoubleSize, 3);
                currentFinalRatio = getCurrentMarketInfo(shareCode).getCurrentPercent();
                if (getCurrentMarketInfo(shareCode).getCurrentPercent()<0){ //加仓情况
                    //下一个周期doubleSize
                    int nextDoubleSize = Math.abs((int)((unitRatio - currentFinalRatio)/ ConstantsUtil.unitHandleRatio))+1;
                    if (nextDoubleSize-currentDoubleSize>0){
                        result.setPreHandleCount((nextDoubleSize-currentDoubleSize)*holdCount/currentDoubleSize);
                        result.setFinalHandleType("b");
                    }
                }else {//减仓情况
                    //下一个周期doubleSize
                    int nextDoubleSize = Math.abs((int)((unitRatio + currentFinalRatio)/ ConstantsUtil.unitHandleRatio))+1;
                    if (currentDoubleSize-nextDoubleSize>0){
                        result.setPreHandleCount((nextDoubleSize-currentDoubleSize)*holdCount/currentDoubleSize);
                        result.setFinalHandleType("s");
                    }
                }

            }
        }
        return result;
    }



    @SneakyThrows
    public MarketRateTheeOutPutDTO getRateThreeIncome(GetRateThreeIncomeInputDTO getRateThreeIncomeInputDTO) {
        MarketRateTheeOutPutDTO returnRateDTO = new MarketRateTheeOutPutDTO();
        String shareCode = getRateThreeIncomeInputDTO.getShareCode();
        returnRateDTO.setMaxShareCode(shareCode);
        getRateThreeIncomeInputDTO.setType(getRateThreeIncomeInputDTO.getShareCode());
        List<QueryRecentSerialRedOutPutDTO> minRateThreeList = serialTempMapper.getMinRateThree(getRateThreeIncomeInputDTO);
        if (minRateThreeList.size() == 0) {
            throw new Exception("因为没有导入行情导致查询行情数据为空");
        }
        //最终收益率
        Double tFinalRatio = getRateThreeIncomeInputDTO.getTFinalRatio();
        //翻倍次数
        Integer doubleSize = 1;
        //实时收益率
        Double runRatio = getRateThreeIncomeInputDTO.getMethodRunRatio();

        Double allProfit = getRateThreeIncomeInputDTO.getAllProfit();

        if (tFinalRatio==0.0&&runRatio==0.0&&allProfit==0.0){
            returnRateDTO.setStartTime(DateUtils.parse(getRateThreeIncomeInputDTO.getQueryStartTime(),DateUtils.DateFormat.YYYY_MM_DD_HH_MM_SS));
        }else {
            returnRateDTO.setStartTime(getRateThreeIncomeInputDTO.getStartDate());
        }

        Boolean firstFlag = true;
        int k= getRateThreeIncomeInputDTO.getK();
        Map<Date,Integer> doubleSizeMap = new HashMap<>();
        for (QueryRecentSerialRedOutPutDTO queryRecentSerialRedOutPutDTO : minRateThreeList) {
            k++;
            shareCode = queryRecentSerialRedOutPutDTO.getShareCode();
            Integer cycleProfit = getRateThreeIncomeInputDTO.getCycleProfit();
            //设定目标收益率不为空 且 总收益率 大于总收益率 且1个单位收益小于刚买入时跌固定目标负收益率
            if (Objects.nonNull(cycleProfit) && tFinalRatio > cycleProfit && runRatio < getRateThreeIncomeInputDTO.getRunRatio())           {
                runRatio = 0.0;
                tFinalRatio = 0.0;
            }
            Double unitProfit = 0.0;//当天的收益
            Double finalRatio = queryRecentSerialRedOutPutDTO.getFinalRatio();
            Double maxRatio = queryRecentSerialRedOutPutDTO.getMaxRatio();
            Double openRatio = queryRecentSerialRedOutPutDTO.getOpenRatio();
            Double fee = getRateThreeIncomeInputDTO.getFee();
            Double fundProfit = getRateThreeIncomeInputDTO.getFundProfit();
            //获取doubleSize
            int i = Math.abs((int) (runRatio / getRateThreeIncomeInputDTO.getDoubleSize())) + 1;
            doubleSizeMap.put(queryRecentSerialRedOutPutDTO.getStartTime(),i);
            doubleSize = i;
            if (k>1){
                firstFlag = false;
            }
           int  unitAmount = 10000;
            Boolean isFinished = false;
            if (firstFlag) {
                if (openRatio + runRatio > fundProfit){
                    unitProfit = MathConstants.Pointkeep(openRatio * unitAmount * 0.01 - unitAmount * fee * 0.01,2);
                    isFinished = true;
                }else if (maxRatio + runRatio > fundProfit) {
                    unitProfit = MathConstants.Pointkeep((fundProfit - runRatio) * unitAmount * 0.01 - unitAmount * fee * 0.01,2);
                    isFinished = true;
                } else {
                    tFinalRatio = tFinalRatio + finalRatio;//减掉0.2 的sxf
                    runRatio = runRatio + finalRatio;
                    unitProfit =  MathConstants.Pointkeep(finalRatio * unitAmount * 0.01,2);
                    allProfit = allProfit +unitProfit;

                }
                returnRateDTO.setAmount(unitProfit);
                returnRateDTO.setMaxShareCode(shareCode);
                returnRateDTO.setEndTime(queryRecentSerialRedOutPutDTO.getStartTime());
                returnRateDTO.setIsFinish(isFinished);
                returnRateDTO.setDoubleSizeMap(doubleSizeMap);
                returnRateDTO.setTFinalRatio(tFinalRatio);
                returnRateDTO.setMethodRunRatio(runRatio);
                returnRateDTO.setAllProfit(allProfit);
                returnRateDTO.setMaxDoubleSize(doubleSize);
                returnRateDTO.setUnitProfit(unitProfit);
                returnRateDTO.setK(k);
                log.info("1QueryStartTime is:{},QueryEndTime is:{},currentTime is:{},shareCode is:{},amount is:{},doubleSize:{}",getRateThreeIncomeInputDTO.getQueryStartTime(),getRateThreeIncomeInputDTO.getQueryEndTime(),queryRecentSerialRedOutPutDTO.getStartTime(),shareCode,returnRateDTO.getAmount(),doubleSize);
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
                returnRateDTO.setDoubleSizeMap(doubleSizeMap);
                returnRateDTO.setTFinalRatio(tFinalRatio);
                returnRateDTO.setMethodRunRatio(runRatio);
                returnRateDTO.setAllProfit(allProfit);
                returnRateDTO.setMaxDoubleSize(doubleSize);
                returnRateDTO.setUnitProfit(MathConstants.Pointkeep((openRatio*doubleSize-fee*doubleSize)*unitAmount*0.01,2));
                returnRateDTO.setK(k);
                log.info("OQueryStartTime is:{},QueryEndTime is:{},currentTime is:{},shareCode is:{},amount is:{},doubleSize:{}",getRateThreeIncomeInputDTO.getQueryStartTime(),getRateThreeIncomeInputDTO.getQueryEndTime(),queryRecentSerialRedOutPutDTO.getStartTime(),shareCode,returnRateDTO.getAmount(),doubleSize);
                return returnRateDTO;
            } else if (maxRatio *doubleSize +tFinalRatio >regularProfit*doubleSize+fee*doubleSize) {
                returnRateDTO.setAmount(MathConstants.Pointkeep((regularProfit*doubleSize)*unitAmount*0.01,2));
                returnRateDTO.setMaxShareCode(shareCode);
                returnRateDTO.setEndTime(queryRecentSerialRedOutPutDTO.getStartTime());
                returnRateDTO.setIsFinish(true);
                returnRateDTO.setDoubleSizeMap(doubleSizeMap);
                returnRateDTO.setTFinalRatio(tFinalRatio);
                returnRateDTO.setMethodRunRatio(runRatio);
                returnRateDTO.setAllProfit(allProfit);
                returnRateDTO.setMaxDoubleSize(doubleSize);
                returnRateDTO.setK(k);
                returnRateDTO.setUnitProfit(MathConstants.Pointkeep((regularProfit*doubleSize-tFinalRatio)*unitAmount*0.01,2));
                log.info("MQueryStartTime is:{},QueryEndTime is:{},currentTime is:{},shareCode is:{},amount is:{},doubleSize:{}",getRateThreeIncomeInputDTO.getQueryStartTime(),getRateThreeIncomeInputDTO.getQueryEndTime(),queryRecentSerialRedOutPutDTO.getStartTime(),shareCode,returnRateDTO.getAmount(),doubleSize);
                return returnRateDTO;
            } else {
                tFinalRatio = tFinalRatio + finalRatio * doubleSize;
                runRatio = runRatio + finalRatio;
                unitProfit = MathConstants.Pointkeep(finalRatio * unitAmount*doubleSize * 0.01,2);
                allProfit = allProfit + unitProfit;
                returnRateDTO.setUnitProfit(unitProfit);
            }

        }
        returnRateDTO.setAmount(tFinalRatio*100);
        returnRateDTO.setMaxDoubleSize(doubleSize);
        returnRateDTO.setIsFinish(false);
        returnRateDTO.setEndTime(DateUtils.parse(getRateThreeIncomeInputDTO.getQueryEndTime(),DateUtils.DateFormat.YYYY_MM_DD_HH_MM_SS));
        returnRateDTO.setDoubleSizeMap(doubleSizeMap);
        returnRateDTO.setTFinalRatio(tFinalRatio);
        returnRateDTO.setMethodRunRatio(runRatio);
        returnRateDTO.setAllProfit(allProfit);
        returnRateDTO.setK(k);
        return returnRateDTO;
    }


    private MarketRealTimeOutputDomain getCurrentMarketInfo(String shareCode){
        MarketInputDomain marketInputDomain = new MarketInputDomain();
        marketInputDomain.setShareCode(shareCode);
        List<MarketRealTimeOutputDomain> outputDomainList = sharesInfoService.getRealTimeRateByXueQiu(marketInputDomain);
        return outputDomainList.get(0);
    }


}
