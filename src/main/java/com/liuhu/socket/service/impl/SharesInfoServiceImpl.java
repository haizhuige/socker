package com.liuhu.socket.service.impl;

import com.liuhu.socket.dao.MarketInfoMapper;
import com.liuhu.socket.dao.ShareInfoMapper;
import com.liuhu.socket.domain.MarketInputDomain;
import com.liuhu.socket.domain.MarketOutputDomain;
import com.liuhu.socket.entity.MarketInfo;
import com.liuhu.socket.enums.SpecialSockerEnum;
import com.liuhu.socket.service.SharesInfoService;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

@Service
public class SharesInfoServiceImpl implements SharesInfoService {
    private static final Logger logger = LogManager.getLogger(SharesInfoServiceImpl.class);

    @Resource
    MarketInfoMapper marketInfoMapper;
    @Resource
    ShareInfoMapper shareInfoMapper;

    @Override
    public List<MarketInfo> getShareInfo(MarketInputDomain input) {
        List<MarketInfo> list = marketInfoMapper.getShareInfo(input);
        return list;
    }

    @Override
    public List<MarketOutputDomain> getRiseOfRate(MarketInputDomain input) {
        List<MarketOutputDomain> outList = new ArrayList<>();
        /**
         * 获取一段时间内的收益率
         */
        List<MarketOutputDomain> list = marketInfoMapper.getLastEndList(input);
        MarketInputDomain aMarket = new MarketInputDomain();
        BeanUtils.copyProperties(input, aMarket);
        aMarket.setShareCode(SpecialSockerEnum.A_SOCKER.getCode());
        MarketOutputDomain aOutPut;
        /**
         * 获取上证指数收益率
         */
        if (input != null && StringUtils.isNotEmpty(input.getShareCode())) {
            aOutPut = marketInfoMapper.getLastEndList(aMarket).get(0);
        } else {
            aOutPut = list.stream().filter(marketOutputDomain -> SpecialSockerEnum.A_SOCKER.getCode().equals(marketOutputDomain.getShareCode())).findFirst().get();
        }
        /**
         * 去掉list中上证指数数据
         */
        for (MarketOutputDomain outPut : list) {
            outPut.setARate(aOutPut.getRate());
            if (!SpecialSockerEnum.A_SOCKER.getCode().equals(outPut.getShareCode())) {
                outList.add(outPut);
            }
        }
        return outList;
    }

    @Override
    public List<MarketOutputDomain> getBuyEveryDay(MarketInputDomain input) {
        List<MarketOutputDomain> outList = new ArrayList<MarketOutputDomain>();
        List<MarketInfo> list = marketInfoMapper.getShareInfo(input);
        Map<String, List<MarketInfo>> map = list.stream().collect(Collectors.groupingBy(MarketInfo::getShareCode));

        for (Entry<String, List<MarketInfo>> entry : map.entrySet()) {
            List<MarketInfo> shareList = entry.getValue();
            MarketOutputDomain output = new MarketOutputDomain();
            output.setShareCode(entry.getKey());
            double mountShare = 0;
            double totalAmount = 0;
            for (int i = 0; i < shareList.size(); i++) {
                MarketInfo market = shareList.get(i);
                if (market.getRiseFall() < 0) {
                    double unitValue = market.getEndValue();
                    mountShare += 100;
                    double amount = unitValue * 100;
                    totalAmount += amount;
                }
            }
            double fixRate = (mountShare * shareList.get(shareList.size() - 1).getEndValue() - totalAmount)
                    / totalAmount;
            output.setFixRate(fixRate);
            double latelyUnit = shareList.get(shareList.size() - 1).getEndValue();
            double earliestUnit = shareList.get(0).getEndValue();
            output.setRate((latelyUnit - earliestUnit) / earliestUnit);
            outList.add(output);
        }
        return outList;
    }

}
