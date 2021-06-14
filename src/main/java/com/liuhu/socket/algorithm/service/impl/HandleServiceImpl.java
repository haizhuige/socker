package com.liuhu.socket.algorithm.service.impl;

import com.liuhu.socket.algorithm.service.HandleService;
import com.liuhu.socket.common.DateUtils;
import com.liuhu.socket.dao.MarketInfoNewMapper;
import com.liuhu.socket.dao.ShareInfoMapper;
import com.liuhu.socket.domain.input.MarketInput2Domain;
import com.liuhu.socket.entity.ShareInfo;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class HandleServiceImpl implements HandleService {

    @Resource
    ShareInfoMapper shareInfoMapper;

    @Resource
    MarketInfoNewMapper marketInfoNewMapper;


    /**
     * 查询随机股票代码某段时间的收益率
     *
     * @param marketInput2Domain
     * @return
     */
    @Override
    public List randomSelectedSocket(MarketInput2Domain marketInput2Domain) {
        if (marketInput2Domain.getCount() == null) {
            marketInput2Domain.setCount(6);
        }
        List<ShareInfo> list = shareInfoMapper.getRandomSocket(marketInput2Domain.getCount());
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList();
        }
        if (marketInput2Domain.getStartTimeDa() == null) {
            String startTime = DateUtils.operateDate(new Date(), -300, DateUtils.DateFormat.YYYY_MM_DD.getFormat());
            Date startTimeDate = DateUtils.parse(startTime, DateUtils.DateFormat.YYYY_MM_DD);
            marketInput2Domain.setStartTimeDa(startTimeDate);
        } else {
            marketInput2Domain.setStartTimeDa(DateUtils.getBeginOfDate(marketInput2Domain.getStartTimeDa()));
        }
        if (marketInput2Domain.getEndTimeDa() == null) {

            marketInput2Domain.setEndTimeDa(new Date());
        }
        List<String> shareCodeList = list.stream().map(ShareInfo::getShareCode).collect(Collectors.toList());
        marketInput2Domain.setShareCodeList(shareCodeList);
        return marketInfoNewMapper.queryPeriodRateByShareCode(marketInput2Domain);
    }

}
