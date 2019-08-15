package com.liuhu.socket.controller;

import com.liuhu.socket.common.DateUtils;
import com.liuhu.socket.common.ResponseResult;
import com.liuhu.socket.domain.MarketInputDomain;
import com.liuhu.socket.domain.MarketOutputDomain;
import com.liuhu.socket.entity.MarketInfo;
import com.liuhu.socket.service.SharesInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;

@Controller
@Slf4j
@RequestMapping("/trade")
public class TradeInfoController {
	
	@Resource
	SharesInfoService sharesInfoService;
	/**
	 * 股票交易操作
	 * @param input
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/operateTrade.do")
	public ResponseResult operateTrade(@RequestBody MarketInputDomain input) {
	    List<MarketInfo> list =	sharesInfoService.getShareInfo(input);
		return ResponseResult.done(list);
	}
}
