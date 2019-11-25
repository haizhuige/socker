package com.liuhu.socket.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.liuhu.socket.common.DateUtils;
import com.liuhu.socket.common.HttpClientUtils;
import com.liuhu.socket.common.MathConstants;
import com.liuhu.socket.dao.MarketInfoMapper;
import com.liuhu.socket.dao.ShareInfoMapper;
import com.liuhu.socket.domain.MarketInputDomain;
import com.liuhu.socket.domain.MarketOutputDomain;
import com.liuhu.socket.dto.SockerExcelEntity;
import com.liuhu.socket.dto.SockerSouhuImportEntity;
import com.liuhu.socket.entity.MarketInfo;
import com.liuhu.socket.entity.MarketInfoNew;
import com.liuhu.socket.entity.ShareInfo;
import com.liuhu.socket.enums.SockerStatusEnum;
import com.liuhu.socket.enums.SpecialSockerEnum;
import com.liuhu.socket.schedule.MarketScheduleService;
import com.liuhu.socket.service.SharesInfoService;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

@Service
public class SharesInfoServiceImpl implements SharesInfoService {
    private static final Logger logger = LogManager.getLogger(SharesInfoServiceImpl.class);

    @Resource
    MarketInfoMapper marketInfoMapper;

    @Resource
    MarketScheduleService scheduleTask;

    @Resource
    ShareInfoMapper shareInfoMapper;

    @Value("${realTime.url}")
    private String realTimeUrl;

    @Override
    public List<MarketInfo> getShareInfo(MarketInputDomain input) {
        List<MarketInfo> list = marketInfoMapper.getShareInfo(input);
        return list;
    }

    /**
     * 查询时间范围内的增长率
     *
     * @param input
     * @return
     */
    @Override
    public List<MarketOutputDomain> getRiseOfRate(MarketInputDomain input) {
        List<MarketOutputDomain> outList = new ArrayList<>();
        /**
         * 获取一段时间内的收益率
         */
        List<MarketOutputDomain> list = getMarketPriodRateInfo(input);
        if (list == null && list.size() <= 0) {
            return new ArrayList<>();
        }
        MarketInputDomain aMarket = new MarketInputDomain();
        BeanUtils.copyProperties(input, aMarket);
        aMarket.setShareCode(SpecialSockerEnum.A_SOCKER.getCode());
        MarketOutputDomain aOutPut;
        /**
         * 获取上证指数收益率
         */
        aOutPut = getMarketPriodRateInfo(aMarket).get(0);
        /**
         * 去掉list中上证指数数据
         */
        for (MarketOutputDomain outPut : list) {
            outPut.setARate(aOutPut.getRateStr());
            if (!SpecialSockerEnum.A_SOCKER.getCode().equals(outPut.getShareCode())) {
                outList.add(outPut);
            }
        }
        /**
         * 根据收益率排序
         */
        if (outList != null) {
            outList = outList.stream().sorted(Comparator.comparing(MarketOutputDomain::getRate).reversed()).collect(Collectors.toList());
        }
        return outList;
    }

    @Override
    public void insertOrUpdateMarketInfo(List<SockerExcelEntity> excelList) {
        if (excelList != null && excelList.size() > 0) {
            marketInfoMapper.insertOrUpdateMarketInfo(excelList);
        }

    }

    @Override
    public Date queryMaxDate(String shareCode) {
        return marketInfoMapper.queryMaxDate(shareCode);
    }

    @Override
    public List<MarketOutputDomain> getRiseOfRateBySohu(MarketInputDomain input) {

        ShareInfo shareInfo = new ShareInfo();
        shareInfo.setStatus(SockerStatusEnum.GROUNDING.getCode());
        List<ShareInfo> shareInfoList = shareInfoMapper.getShareInfoWithoutASocker(shareInfo);
        List<MarketOutputDomain> outputDomainList = new ArrayList<>();
        input.setShareCode(SpecialSockerEnum.A_SOCKER.getCode());
        MarketOutputDomain aSockerDomain = resolvingData(input);
        if (aSockerDomain == null) {
            return new ArrayList<>();
        }
        for (ShareInfo result : shareInfoList) {
            input.setShareCode(result.getShareCode());
            MarketOutputDomain outputDomain = resolvingData(input);
            if (outputDomain == null) {
                continue;
            }
            outputDomain.setARate(aSockerDomain.getRateStr());
            outputDomain.setShareName(result.getShareName());
            outputDomainList.add(outputDomain);
        }
        outputDomainList = outputDomainList.stream().sorted(Comparator.comparing(MarketOutputDomain::getRate).reversed()).collect(Collectors.toList());
        return outputDomainList;
    }

    @Override
    public List<MarketOutputDomain> getRealTimeRateByWangyi() {
        ShareInfo shareInfo = new ShareInfo();
        shareInfo.setStatus(SockerStatusEnum.GROUNDING.getCode());
        String shareCodes = shareInfoMapper.getRealTimeRateByWangyi(shareInfo);
        String newUrl = realTimeUrl + shareCodes;
        String response = HttpClientUtils.getXpath(newUrl);
        String jsonStr = response.substring(response.indexOf("(") + 1, response.indexOf(")"));
        JSONObject obj = JSONObject.parseObject(jsonStr);
        Collection<Object> collection = obj.values();
        Iterator it = collection.iterator();
        List<MarketOutputDomain> list = new ArrayList<>();
        while (it.hasNext()) {
            Object object = it.next();
            String valueStr = JSONObject.toJSONString(object);
            JSONObject valueObject = JSONObject.parseObject(valueStr);
            MarketOutputDomain outputDomain = new MarketOutputDomain();
            outputDomain.setShareCode(valueObject.getString("code"));
            outputDomain.setShareName(valueObject.getString("name"));
            double updowm = valueObject.getDouble("updown");
            double yestclose = valueObject.getDouble("yestclose");
            outputDomain.setRate(MathConstants.Pointkeep(new BigDecimal(updowm).divide(new BigDecimal(yestclose), 2).doubleValue(), 4));
            outputDomain.setRateStr(MathConstants.Pointkeep(outputDomain.getRate() * 100, 4) + "%");
            list.add(outputDomain);
        }
        list = list.stream().sorted(Comparator.comparing(MarketOutputDomain::getRate).reversed()).collect(Collectors.toList());
        return list;

    }

    @Override
    public List<MarketOutputDomain> getDimentionRate(MarketInputDomain inputDomain) {
        try {
            SockerSouhuImportEntity entity = scheduleTask.getMarketJsonBySouhu(inputDomain);
            List<List<String>> hqList = entity.getHq();
            List<MarketOutputDomain> outputDomainList = new ArrayList<>();
            MarketInputDomain sockerAInputDomain = new MarketInputDomain();
            BeanUtils.copyProperties(inputDomain, sockerAInputDomain);
            //查询A股数据
            sockerAInputDomain.setShareCode(SpecialSockerEnum.A_SOCKER.getCode());
            SockerSouhuImportEntity sockerAEntity = scheduleTask.getMarketJsonBySouhu(sockerAInputDomain);
            List<List<String>> sockerAList = sockerAEntity.getHq();
            double sumRate = 0;//个股汇总
            double sumARate = 0;//a股汇总
            for (int i = 0; i < hqList.size(); i++) {
                List<String> list = hqList.get(i);
                MarketOutputDomain outputDomain = new MarketOutputDomain();
                BeanUtils.copyProperties(inputDomain, outputDomain);
                String ratio = list.get(4);
                outputDomain.setRateStr(ratio);
                sumRate += Double.parseDouble(ratio.replace("%",""));
                outputDomain.setStartTime(list.get(0));
                for (int j = 0; j < sockerAList.size(); j++) {
                    List<String> alist = sockerAList.get(j);
                    if (i == j) {
                        outputDomain.setARate(alist.get(4));
                        sumARate +=Double.parseDouble(outputDomain.getARate().replace("%",""));
                        break;
                    }
                }
                outputDomainList.add(outputDomain);

            }
            if (outputDomainList.size()>0){
                MarketOutputDomain sumRateOutDomain = new MarketOutputDomain();
                BeanUtils.copyProperties(outputDomainList.get(0),sumRateOutDomain);
                sumRateOutDomain.setARate(MathConstants.Pointkeep(sumARate,2)+"%");
                sumRateOutDomain.setRateStr(MathConstants.Pointkeep(sumRate,2)+"%");
                sumRateOutDomain.setStartTime("汇总");
                outputDomainList.add(sumRateOutDomain);
            }

            return outputDomainList;
        } catch (IOException e) {
            logger.error("从搜狐股票获取数据失败");
            return new ArrayList<>();
        }

    }

    @Override
    public List<ShareInfo> getShareInfo() {
        ShareInfo shareInfo = new ShareInfo();
        shareInfo.setStatus(SockerStatusEnum.GROUNDING.getCode());
        return shareInfoMapper.getShareInfoWithoutASocker(shareInfo);
    }

    private List<MarketOutputDomain> getMarketPriodRateInfo(MarketInputDomain input) {
        List<MarketOutputDomain> returnList = new ArrayList<>();
        List<MarketInfo> lastEndList = marketInfoMapper.getLastEndList(input);
        Map<String, List<MarketInfo>> map = lastEndList.stream().collect(Collectors.groupingBy(MarketInfo::getShareCode));
        for (Entry<String, List<MarketInfo>> entry : map.entrySet()) {
            List<MarketInfo> list = entry.getValue();
            MarketOutputDomain marketOutputDomain = new MarketOutputDomain();
            if (list.size() == 1) {
                MarketInfo info = list.get(0);
                BeanUtils.copyProperties(info, marketOutputDomain);
                marketOutputDomain.setRate(Double.parseDouble(info.getRiseFallRatio()));
                marketOutputDomain.setRateStr(MathConstants.ParseStrPointKeep(info.getRiseFallRatio(), 2) + "%");
                marketOutputDomain.setStartTime(DateUtils.format(info.getDate(), DateUtils.DateFormat.YYYY_MM_DD));
                marketOutputDomain.setEndTime(DateUtils.format(info.getDate(), DateUtils.DateFormat.YYYY_MM_DD));
            } else {
                MarketInfo maxInfo = list.get(0);
                MarketInfo minInfo = list.get(1);
                if (maxInfo.getDate().compareTo(minInfo.getDate()) < 0) {
                    return null;
                }
                BeanUtils.copyProperties(maxInfo, marketOutputDomain);
                double rate = 0;
                if (maxInfo.getTotalAmount() > 0) {
                    double preAllAmount = minInfo.getTotalAmount() / (1 + Double.parseDouble(minInfo.getRiseFallRatio()) * 0.01);
                    // logger.info("查询数据preAllAmount={}，maxInfo.getTotalAmount={},maxInfo={}",preAllAmount,maxInfo.getTotalAmount(),maxInfo.getShareCode());
                    rate = MathConstants.Pointkeep((maxInfo.getTotalAmount() - preAllAmount) / preAllAmount, 4);
                } else {
                    rate = MathConstants.Pointkeep((maxInfo.getEndValue() - minInfo.getPreEndValue()) / minInfo.getPreEndValue(), 4);
                }
                marketOutputDomain.setRate(rate);
                marketOutputDomain.setRateStr(MathConstants.Pointkeep(rate * 100, 4) + "%");
                marketOutputDomain.setStartTime(DateUtils.format(minInfo.getDate(), DateUtils.DateFormat.YYYY_MM_DD));
                marketOutputDomain.setEndTime(DateUtils.format(maxInfo.getDate(), DateUtils.DateFormat.YYYY_MM_DD));
            }
            returnList.add(marketOutputDomain);
        }
        return returnList;
    }

    private MarketOutputDomain resolvingData(MarketInputDomain inputDomain) {
        MarketOutputDomain outputDomain = new MarketOutputDomain();
        SockerSouhuImportEntity entity;
        try {
            entity = scheduleTask.getMarketJsonBySouhu(inputDomain);
            if (entity == null) {
                return null;
            }
            List<String> list = entity.getStat();
            String ratio = list.get(3);
            BeanUtils.copyProperties(inputDomain, outputDomain);
            outputDomain.setRateStr(ratio);
            if (ratio.contains("%")) {
                outputDomain.setRate(Double.parseDouble(ratio.replace("%", "")));
            }
            return outputDomain;
        } catch (IOException e) {
            logger.error("查询搜狐获取行情失败，e={}", e);
            return new MarketOutputDomain();
        }

    }


}
