package com.liuhu.socket.controller;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import com.liuhu.socket.common.DateUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.liuhu.socket.common.ResponseResult;
import com.liuhu.socket.domain.MarketInputDomain;
import com.liuhu.socket.domain.MarketOutputDomain;
import com.liuhu.socket.entity.MarketInfo;
import com.liuhu.socket.service.SharesInfoService;

@Controller
public class ShareController {
	
	@Resource
	SharesInfoService sharesInfoService;
	/**
	 * 查询一段时间范围内的行情数据
	 * @param input
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/getShareInfo.do")
	public ResponseResult getShareInfo(@RequestBody MarketInputDomain input) {
	    List<MarketInfo> list =	sharesInfoService.getShareInfo(input);
		return ResponseResult.done(list);
	} 
    /**
     * 查询一段时间范围内的增长率
     */
	@ResponseBody
	@RequestMapping("/getRiseOfRate.do")
	public ResponseResult getRiseOfRate(@RequestBody MarketInputDomain input) {
        String startTime = DateUtils.format(input.getStartTimeDa(),DateUtils.DateFormat.YYYY_MM_DD);
        String endTime =DateUtils.format(input.getEndTimeDa(),DateUtils.DateFormat.YYYY_MM_DD);
        input.setStartTime(startTime);
        input.setEndTime(endTime);
		List<MarketOutputDomain> rateList =	sharesInfoService.getRiseOfRate(input);
		return ResponseResult.done(rateList);
	} 
	/**
	 * 查询下跌买1手的收益
	 */
	@ResponseBody
	@RequestMapping("/getBuyEveryDay.do")
	public ResponseResult getBuyEveryDay(@RequestBody MarketInputDomain input) {
		List<MarketOutputDomain> rateList =	sharesInfoService.getBuyEveryDay(input);
		return ResponseResult.done(rateList);
	} 
}
