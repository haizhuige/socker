package com.liuhu.socket.service.impl;

import com.liuhu.socket.common.DateUtils;
import com.liuhu.socket.dao.ShareInfoMapper;
import com.liuhu.socket.dao.TradeDetailInfoMapper;
import com.liuhu.socket.domain.input.HandleSockerInnerInputDomain;
import com.liuhu.socket.domain.input.HandleSockerInputDomain;
import com.liuhu.socket.domain.input.TradeInputDomain;
import com.liuhu.socket.domain.output.HandleSockerTargetPriceOutDomain;
import com.liuhu.socket.entity.ShareInfo;
import com.liuhu.socket.entity.TradeInfoDetail;
import com.liuhu.socket.service.PersonalTradeInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PersonalTradeInfoServiceImpl implements PersonalTradeInfoService {

    @Autowired
    TradeDetailInfoMapper tradeDetailInfoMapper;

    @Autowired
    ShareInfoMapper shareInfoMapper;

    @Override
    public int operateTrade(TradeInputDomain input) {

        TradeInfoDetail tradeInfoDetail = new TradeInfoDetail();
        tradeInfoDetail.setBuyCount(input.getHandNum());
        tradeInfoDetail.setBuyPrice(input.getUnitValue());
        tradeInfoDetail.setShareCode(input.getShareCode());
        tradeInfoDetail.setBuyDate(DateUtils.parse(input.getInvestTime(),DateUtils.DateFormat.YYYY_MM_DD_HH_MM_SS));
        ShareInfo shareInfo = new ShareInfo();
        shareInfo.setShareCode(input.getShareCode());
        List<ShareInfo> shareInfoList = shareInfoMapper.getShareInfo(shareInfo);
        String shareName = shareInfoList.get(0).getShareName();
        tradeInfoDetail.setShareName(shareName);
        tradeDetailInfoMapper.insertSelective(tradeInfoDetail);
        return 0;
    }

    @Override
    public List<HandleSockerTargetPriceOutDomain> getHandlePrice(HandleSockerInputDomain handleSockerInputDomain) {
        List<HandleSockerInnerInputDomain> socketList = handleSockerInputDomain.getSocketList();
        //如果入参为空，则查询detail表中的数据获取最新当前下一个交易日的操作价格
        if (socketList.isEmpty()){
            TradeInfoDetail tradeInfoDetail = new TradeInfoDetail();
            tradeInfoDetail.setIsAble("0");//持有中
            List<TradeInfoDetail> tradeInfoDetailList =  tradeDetailInfoMapper.queryDetailInfoByCondition(tradeInfoDetail);
            for (TradeInfoDetail detail:tradeInfoDetailList){
                Double buyPrice = detail.getBuyPrice();
            }
        }

        return null;
    }
}
