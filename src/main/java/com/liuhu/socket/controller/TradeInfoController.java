package com.liuhu.socket.controller;

import com.liuhu.socket.common.ResponseResult;
import com.liuhu.socket.domain.input.MarketDetailInputDomain;
import com.liuhu.socket.domain.input.QueryRecentSerialRedConditionDTO;
import com.liuhu.socket.domain.input.TradeInputDomain;
import com.liuhu.socket.domain.output.MarketOutputDomain;
import com.liuhu.socket.domain.output.MarketRateTheeOutPutDTO;
import com.liuhu.socket.domain.output.QueryRecentSerialRedOutPutDTO;
import com.liuhu.socket.entity.TradeDateInfo;
import com.liuhu.socket.service.TradeInfoService;
import com.liuhu.socket.service.TradeMethodService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Controller
@Slf4j
@RequestMapping("/trade")
public class TradeInfoController {
	
	@Resource
    TradeInfoService tradeInfoService;

	@Resource
	TradeMethodService tradeMethodService;
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
     * 获取换手率为3的收益率
     * @param type
     * @return
     */
    @ResponseBody
    @RequestMapping("/getRateThreeIncome")
    public ResponseResult getRateThreeIncome(@RequestParam Integer type) {
        MarketRateTheeOutPutDTO marketRateTheeOutPutDTO =	tradeInfoService.getRateThreeIncome(type);
        return ResponseResult.done(marketRateTheeOutPutDTO);
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

    /**
     * 获取当日待考虑socker
     * @param input
     * @return
     */
    @ResponseBody
    @RequestMapping("/getPrePurchaseSocker")
    public ResponseResult getPrePurchaseSocker(@RequestBody QueryRecentSerialRedConditionDTO input) {
        List<MarketOutputDomain> outputDomain = tradeInfoService.getPrePurchaseSocker(input);
         return ResponseResult.done(outputDomain);
    }


	/**
	 * 获取当日待考虑socker
	 * @param input
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/getSerialRedThree")
	public ResponseResult getSerialRedThree(@RequestBody QueryRecentSerialRedConditionDTO input) {
		List<QueryRecentSerialRedOutPutDTO> outputDomain = tradeMethodService.getRecentFinalRatioStrategy(input);
		return ResponseResult.done(outputDomain);
	}


	/**
	 * 获取当日待考虑socker
	 * @param input
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/getFiveUpRatio")
	public ResponseResult getFiveUpRatio(@RequestBody QueryRecentSerialRedConditionDTO input) {
		Map<String,Object> resultMap = tradeInfoService.getPreFiveAndSubFive(input);
		return ResponseResult.done(resultMap);
	}

    /**
     * 查询v型反转shouyi
     * @param input
     * @return
     */
    @ResponseBody
    @RequestMapping("/queryVRatioFromDownStartPoint")
    public ResponseResult queryVRatioFromDownStartPoint(@RequestBody QueryRecentSerialRedConditionDTO input) throws Exception {
        List<QueryRecentSerialRedOutPutDTO> outPutDTOList = tradeMethodService.queryVRatioFromDownStartPoint(input);
        return ResponseResult.done(outPutDTOList);
    }

}
