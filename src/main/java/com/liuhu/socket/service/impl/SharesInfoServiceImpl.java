package com.liuhu.socket.service.impl;

import com.liuhu.socket.common.DateUtils;
import com.liuhu.socket.common.MathConstants;
import com.liuhu.socket.dao.MarketInfoMapper;
import com.liuhu.socket.dao.ShareInfoMapper;
import com.liuhu.socket.domain.MarketInputDomain;
import com.liuhu.socket.domain.MarketOutputDomain;
import com.liuhu.socket.dto.SockerExcelEntity;
import com.liuhu.socket.entity.MarketInfo;
import com.liuhu.socket.enums.SpecialSockerEnum;
import com.liuhu.socket.service.SharesInfoService;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

@Service
public class SharesInfoServiceImpl implements SharesInfoService {
    private static final Logger logger = LogManager.getLogger(SharesInfoServiceImpl.class);

    @Resource
    MarketInfoMapper marketInfoMapper;

    @Override
    public List<MarketInfo> getShareInfo(MarketInputDomain input) {
        List<MarketInfo> list = marketInfoMapper.getShareInfo(input);
        return list;
    }
    /**
     * 查询时间范围内的增长率
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
}
