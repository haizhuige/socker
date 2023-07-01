package com.liuhu.socket.controller;

import com.liuhu.socket.common.ResponseResult;
import com.liuhu.socket.domain.input.MarketDetailInputDomain;
import com.liuhu.socket.domain.input.TradeInputDomain;
import com.liuhu.socket.entity.TradeDateInfo;
import com.liuhu.socket.service.TradeInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Map;

@Controller
@Slf4j
@RequestMapping("/trade")
public class TradeInfoController {
	
	@Resource
    TradeInfoService tradeInfoService;
	/**
	 * 股票购买操作
	 * @param input
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/operateTrade.do")
	public ResponseResult operateTrade(@RequestBody TradeInputDomain input) {
	    int a =	tradeInfoService.operateTrade(input);
		return ResponseResult.done(a);
	}
    /**
     * 股票售卖操作
     * @param input
     * @return
     */
    @ResponseBody
    @RequestMapping("/saleTrade.do")
    public ResponseResult saleTrade(@RequestBody TradeInputDomain input) {
        int a =	tradeInfoService.saleTrade(input);
        return ResponseResult.done(a);
    }

	/**
	 * 长期持有股票收益
	 * @param input
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/ownerLongIncome.do")
	public ResponseResult ownerLongIncome(@RequestBody MarketDetailInputDomain input) {
		Map a =	tradeInfoService.ownerLongIncome(input);
		return ResponseResult.done(a);
	}

	/**
	 * 当前最大交易时间
	 * @param input
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/getMaxDate")
	public ResponseResult getMaxDate(@RequestBody MarketDetailInputDomain input) {
		TradeDateInfo date = tradeInfoService.queryMaxDate();
		return ResponseResult.done(date);
	}
}
