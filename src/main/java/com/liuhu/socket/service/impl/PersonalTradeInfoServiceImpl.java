package com.liuhu.socket.service.impl;

import com.liuhu.socket.common.DateUtils;
import com.liuhu.socket.dao.ShareInfoMapper;
import com.liuhu.socket.dao.TradeCollectInfoMapper;
import com.liuhu.socket.domain.input.CalNextSaleOrBuyInputDTO;
import com.liuhu.socket.domain.input.HandleSockerInnerInputDomain;
import com.liuhu.socket.domain.input.HandleSockerInputDomain;
import com.liuhu.socket.domain.input.TradeInputDomain;
import com.liuhu.socket.domain.output.HandleSockerTargetPriceOutDomain;
import com.liuhu.socket.entity.ShareInfo;
import com.liuhu.socket.entity.TradeCollectInfo;
import com.liuhu.socket.service.PersonalTradeInfoService;
import com.liuhu.socket.service.TradeInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PersonalTradeInfoServiceImpl implements PersonalTradeInfoService {

    @Autowired
    TradeCollectInfoMapper tradeCollectInfoMapper;

    @Autowired
    ShareInfoMapper shareInfoMapper;

    @Autowired
    TradeInfoService tradeInfoService;

    @Override
    public int operateTrade(TradeInputDomain input) {

        TradeCollectInfo tradeCollectInfo = new TradeCollectInfo();
        tradeCollectInfo.setBuyCount(input.getHandNum());
        tradeCollectInfo.setBuyPrice(input.getUnitValue());
        tradeCollectInfo.setShareCode(input.getShareCode());
        tradeCollectInfo.setBuyDate(DateUtils.parse(input.getInvestTime(),DateUtils.DateFormat.YYYY_MM_DD_HH_MM_SS));
        ShareInfo shareInfo = new ShareInfo();
        shareInfo.setShareCode(input.getShareCode());
        List<ShareInfo> shareInfoList = shareInfoMapper.getShareInfo(shareInfo);
        String shareName = shareInfoList.get(0).getShareName();
        tradeCollectInfo.setShareName(shareName);
        tradeCollectInfoMapper.insertSelective(tradeCollectInfo);
        return 0;
    }

    @Override
    public List<HandleSockerTargetPriceOutDomain> getHandlePrice(HandleSockerInputDomain handleSockerInputDomain) {
        List<HandleSockerInnerInputDomain> socketList = handleSockerInputDomain.getSocketList();
        //如果入参为空，则查询detail表中的数据获取最新当前下一个交易日的操作价格
        if (socketList.isEmpty()){
            TradeCollectInfo tradeCollectInfo = new TradeCollectInfo();
            tradeCollectInfo.setIsAble("0");//持有中
            List<TradeCollectInfo> tradeCollectInfoList =  tradeCollectInfoMapper.queryDetailInfoByCondition(tradeCollectInfo);
            for (TradeCollectInfo collectInfo: tradeCollectInfoList){
                CalNextSaleOrBuyInputDTO calNextSaleOrBuyInputDTO = new CalNextSaleOrBuyInputDTO();
                calNextSaleOrBuyInputDTO.setCostPrice(collectInfo.getBuyPrice());
                calNextSaleOrBuyInputDTO.setCurrentDoubleSize(collectInfo.getDoubleSize());
                calNextSaleOrBuyInputDTO.setTFinalRatio(collectInfo.getSumRatio());
                calNextSaleOrBuyInputDTO.setHoldCount(collectInfo.getBuyCount());
                calNextSaleOrBuyInputDTO.setShareCode(collectInfo.getShareCode());
                calNextSaleOrBuyInputDTO.setStartTime(DateUtils.format(collectInfo.getBuyDate(),DateUtils.DateFormat.YYYY_MM_DD_HH_MM_SS));
                tradeInfoService.getNextHandleSharePrice(calNextSaleOrBuyInputDTO);
            }
        }

        return null;
    }



}
