package com.liuhu.socket.controller;

import com.alibaba.fastjson.JSONObject;
import com.liuhu.socket.algorithm.service.HandleService;
import com.liuhu.socket.common.DateUtils;
import com.liuhu.socket.common.MathConstants;
import com.liuhu.socket.common.ResponseResult;
import com.liuhu.socket.domain.input.MarketInput2Domain;
import com.liuhu.socket.domain.input.MarketInputDomain;
import com.liuhu.socket.domain.input.QueryRecentSerialRedConditionDTO;
import com.liuhu.socket.domain.output.MarketOutputDomain;
import com.liuhu.socket.domain.input.NetIncomeInputDomain;
import com.liuhu.socket.domain.output.QueryRecentSerialRedOutPutDTO;
import com.liuhu.socket.entity.MarketInfo;
import com.liuhu.socket.enums.TradeStatusEnum;
import com.liuhu.socket.service.SharesInfoService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;

@Controller
public class MarketInfoController {

    @Resource
    SharesInfoService sharesInfoService;

    @Resource
    HandleService handleService;
    /**
     * 查询一段时间范围内的行情数据
     *
     * @param input
     * @return
     */
    @ResponseBody
    @RequestMapping("/getShareInfo.do")
    public ResponseResult getShareInfo(@RequestBody MarketInputDomain input) {
        List<MarketInfo> list = sharesInfoService.getShareInfo(input);
        return ResponseResult.done(list);
    }

    /**
     * 查询一段时间范围内的增长率
     */
    @ResponseBody
    @RequestMapping("/getRiseOfRate.do")
    public ResponseResult getRiseOfRate(@RequestBody MarketInputDomain input) {
        String startTime = DateUtils.format(input.getStartTimeDa(), DateUtils.DateFormat.YYYY_MM_DD);
        String endTime = DateUtils.format(input.getEndTimeDa(), DateUtils.DateFormat.YYYY_MM_DD);
        input.setStartTime(startTime);
        input.setEndTime(endTime);
        List<MarketOutputDomain> rateList = sharesInfoService.getRiseOfRateBySohu(input);
        return ResponseResult.done(rateList);
    }

    /**
     * 查询实时股票增长率
     */
    @ResponseBody
    @RequestMapping("/getRealTimeRateByWangyi.do")
    public ResponseResult getRealTimeRateByWangyi() {
        List<MarketOutputDomain> rateList = sharesInfoService.getRealTimeRateByWangyi();
        return ResponseResult.done(rateList);
    }

    /**
     * 以周、月为维度统计股票增长率信息
     */
    @ResponseBody
    @RequestMapping("/getDimentionRate.do")
    public ResponseResult getDimentionRate(@RequestBody MarketInputDomain input) {
        if (input == null || StringUtils.isEmpty(input.getShareCode()) || StringUtils.isEmpty(input.getPeriod()) || input.getStartTimeDa()==null || input.getEndTimeDa()==null) {
            return ResponseResult.failed("入参为空");
        }
        String startTime = DateUtils.format(input.getStartTimeDa(), DateUtils.DateFormat.YYYY_MM_DD);
        String endTime = DateUtils.format(input.getEndTimeDa(), DateUtils.DateFormat.YYYY_MM_DD);
        input.setStartTime(startTime);
        input.setEndTime(endTime);
        if (StringUtils.isEmpty(input.getStartTime()) || StringUtils.isEmpty(input.getEndTime())) {
            return ResponseResult.failed("日期传入为空");
        }
        String shareCode = input.getShareCode();
        String[] taskArray = shareCode.split("\\|");
        input.setShareCode(taskArray[0]);
        input.setShareName(taskArray[1]);
        List<MarketOutputDomain> rateList = sharesInfoService.getDimentionRate(input);
        return ResponseResult.done(rateList);
    }

    /**
     * 查询买入卖出净收入
     */
    @ResponseBody
    @RequestMapping("/calNetIncome.do")
    public ResponseResult calNetIncome(@RequestBody NetIncomeInputDomain input) {
        //购买费率
        double buyCommission = MathConstants.computerCommission(input.getBuyUnit(), input.getSaleNum(), TradeStatusEnum.BUY.getCode());
        //卖出费率
        double saleCommission = MathConstants.computerCommission(input.getSaleUnit(), input.getSaleNum(), TradeStatusEnum.SALE.getCode());
        //净收入 卖出价-买入价-费率
        double netIncome = MathConstants.Pointkeep((input.getSaleUnit() - input.getBuyUnit()) * input.getSaleNum() - buyCommission - saleCommission, 2);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("netIncome", netIncome);
        jsonObject.put("saleCommission", saleCommission);
        jsonObject.put("buyCommission", buyCommission);
        return ResponseResult.done(jsonObject);
    }

    /**
     * 查询随机code
     */
    @ResponseBody
    @RequestMapping("/queryRandomRatio.do")
    public ResponseResult queryRandomRatio(@RequestBody MarketInput2Domain input2Domain) {

        List list = handleService.randomSelectedSocket(input2Domain);
        return ResponseResult.done(list);
    }

    /**
     * 查询连续下跌之后又连续上涨的股票及其收益率
     * (连续下跌固定数，上涨区间固定几个，)
     */
    @ResponseBody
    @RequestMapping("/queryRecentSerialRed.do")
    public ResponseResult queryRecentSerialRed(@RequestBody QueryRecentSerialRedConditionDTO input2Domain) throws Exception {

        List<QueryRecentSerialRedOutPutDTO> list = sharesInfoService.queryRecentSerialRed(input2Domain);
        return ResponseResult.done(list);
    }


    /**
     * 查询连续下跌之后又连续上涨的股票及其收益率
     * (连续下跌固定数，上涨区间根据最小上涨数而定，比如上涨区间有三次上涨，则以下一个交易日作为起点purchase)
     */
    @ResponseBody
    @RequestMapping("/queryRecentSerialMinPurchase.do")
    public ResponseResult queryRecentSerialMinPurchase(@RequestBody QueryRecentSerialRedConditionDTO input2Domain) throws Exception {

        List<QueryRecentSerialRedOutPutDTO> list = sharesInfoService.queryRecentSerialMinPurchase(input2Domain);
        return ResponseResult.done(list);
    }


    @ResponseBody
    @RequestMapping("/getSerialDownAndThenMarket")
    public ResponseResult getSerialDownAndThenMarket() {
        List<QueryRecentSerialRedOutPutDTO> rateList = sharesInfoService.getSerialDownAndThenMarket();
        return ResponseResult.done(rateList);
    }

}
