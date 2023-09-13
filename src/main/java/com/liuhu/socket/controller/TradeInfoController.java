package com.liuhu.socket.controller;

import com.liuhu.socket.common.ResponseResult;
import com.liuhu.socket.domain.input.*;
import com.liuhu.socket.domain.output.MarketOutputDomain;
import com.liuhu.socket.domain.output.MarketRateTheeOutPutDTO;
import com.liuhu.socket.domain.output.QueryFixSerialDownOutDTO;
import com.liuhu.socket.domain.output.QueryRecentSerialRedOutPutDTO;
import com.liuhu.socket.entity.TradeDateInfo;
import com.liuhu.socket.service.TradeInfoService;
import com.liuhu.socket.service.TradeMethodService;
import com.liuhu.socket.service.TradeMethodStrategyConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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

	@Autowired
	@Qualifier("moRen")
	TradeMethodService tradeMethodService;


	@Resource
	TradeMethodStrategyConfig tradeMethodStrategyConfig;
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
     * @param getRateThreeIncomeInputDTO
     * @return
     */
    @ResponseBody
    @RequestMapping("/getRateThreeIncome")
    public ResponseResult getRateThreeIncome(@RequestBody GetRateThreeIncomeInputDTO getRateThreeIncomeInputDTO) {
		Map<String, TradeMethodService> tradeImpl = tradeMethodStrategyConfig.getTradeImpl();
		String methodType = getRateThreeIncomeInputDTO.getMethodType();
		TradeMethodService tradeMethodService = tradeImpl.get(methodType);
		MarketRateTheeOutPutDTO marketRateTheeOutPutDTO =	tradeMethodService.getRateThreeIncome(getRateThreeIncomeInputDTO);
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

	/**
	 *
	 * @param input
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/queryThreeDownThen")
	public ResponseResult queryThreeDownThen(@RequestBody QueryRecentSerialRedConditionDTO input) throws Exception {
        Map<String, TradeMethodService> tradeImpl = tradeMethodStrategyConfig.getTradeImpl();
        String methodType = input.getMethodType();
        TradeMethodService tradeMethodService = tradeImpl.get(methodType);
		List<QueryRecentSerialRedOutPutDTO> outPutDTOList = tradeMethodService.queryThreeDownRatioByDate(input);
		return ResponseResult.done(outPutDTOList);
	}


	/**
	 *  前两周跌,三连涨之后行情走势
	 * @param input
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/queryThreeUpThenAndPreDownRegular")
	public ResponseResult queryThreeUpThenAndPreDownRegular(@RequestBody QueryRecentSerialRedConditionDTO input) throws Exception {
		Map<String, TradeMethodService> tradeImpl = tradeMethodStrategyConfig.getTradeImpl();
		String methodType = input.getMethodType();
		TradeMethodService tradeMethodService = tradeImpl.get(methodType);
		List<QueryRecentSerialRedOutPutDTO> outPutDTOList = tradeMethodService.queryThreeUpThenAndPreDownRegular(input);
		return ResponseResult.done(outPutDTOList);
	}

	/**
	 *  查询前一段时间跌,首次大涨之后五天的行情
	 * @param input
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/queryPreDownAndFirstUpThenFiveMarket")
	public ResponseResult queryPreDownAndFirstUpThenFiveMarket(@RequestBody GetRateThreeIncomeInputDTO input) throws Exception {
		Map<String, TradeMethodService> tradeImpl = tradeMethodStrategyConfig.getTradeImpl();
		String methodType = input.getMethodType();
		TradeMethodService tradeMethodService = tradeImpl.get(methodType);
		List<QueryRecentSerialRedOutPutDTO> outPutDTOList = tradeMethodService.queryFirstBuyMoreThenMarketRatio(input);
		return ResponseResult.done(outPutDTOList);
	}

	/**
	 * 查询一段时间内连续down超过5%的
	 */
	@ResponseBody
	@RequestMapping("/getFixSerialDown")
	public ResponseResult getFixSerialDown(@RequestBody QueryProfitByComProgram queryProfitByComProgram) throws Exception {

		Map<Date, List<String>> fixSerialDown = tradeInfoService.getFixSerialDown(queryProfitByComProgram);
		return ResponseResult.done(fixSerialDown);
	}


	/**
	 * 查询一段时间内连续down超过5%的操作收益
	 */
	@ResponseBody
	@RequestMapping("/getFixSerialDownProfit")
	public ResponseResult getFixSerialDownProfit(@RequestBody QueryProfitByComProgram queryProfitByComProgram) throws Exception {

		MarketOutputDomain profitFromSerialDown = tradeInfoService.getProfitFromSerialDown(queryProfitByComProgram);
		return ResponseResult.done(profitFromSerialDown);
	}

	/**
	 * 查询一段时间内连续down超过5%的
	 */
	@ResponseBody
	@RequestMapping("/getPreSelectionShareInfo")
	public ResponseResult getPreSelectionShareInfo(@RequestBody QueryFixSerialDownInDTO queryProfitByComProgram) throws Exception {

		List<QueryFixSerialDownOutDTO> preSelectionSerialDownDTOList = tradeInfoService.getPreSelectionSerialDownDTOList(queryProfitByComProgram);
		return ResponseResult.done(preSelectionSerialDownDTOList);
	}

	/**
	 * 查询一段时间内连续水下的
	 */
	@ResponseBody
	@RequestMapping("/preSelectionGetSerialDownOfRiver")
	public ResponseResult preSelectionGetSerialDownOfRiver(@RequestBody QueryFixSerialDownInDTO queryProfitByComProgram) throws Exception {

		List<QueryFixSerialDownOutDTO> preSelectionSerialDownDTOList = tradeInfoService.preSelectionGetSerialDownOfRiver(queryProfitByComProgram);
		return ResponseResult.done(preSelectionSerialDownDTOList);
	}

}
