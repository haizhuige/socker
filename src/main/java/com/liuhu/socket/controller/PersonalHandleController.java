package com.liuhu.socket.controller;

import com.liuhu.socket.common.ResponseResult;
import com.liuhu.socket.domain.input.HandleSockerInputDomain;
import com.liuhu.socket.domain.input.TradeInputDomain;
import com.liuhu.socket.domain.output.HandleSockerTargetPriceOutDomain;
import com.liuhu.socket.service.PersonalTradeInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@Slf4j
@RequestMapping("/handle")
public class PersonalHandleController {


    @Autowired
    PersonalTradeInfoService personalTradeInfoService;

    /**
     * 个人购买操作
     * @param input
     * @return
     */
    @ResponseBody
    @RequestMapping("/operateTrade.do")
    public ResponseResult operateTrade(@RequestBody TradeInputDomain input) {
        int a =	personalTradeInfoService.operateTrade(input);
        return ResponseResult.done(a);
    }


    /**
     * 获取待操作personalHandlePrice
     * @param handleSockerInputDomain
     * @return
     */
    @ResponseBody
    @RequestMapping("/getHandlePrice.do")
    public ResponseResult getHandlePrice(@RequestBody HandleSockerInputDomain handleSockerInputDomain) {
        List<HandleSockerTargetPriceOutDomain> handlePriceList = personalTradeInfoService.getHandlePrice(handleSockerInputDomain);
        return ResponseResult.done(handlePriceList);
    }


}
