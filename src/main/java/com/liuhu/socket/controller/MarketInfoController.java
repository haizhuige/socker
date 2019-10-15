package com.liuhu.socket.controller;

import com.alibaba.fastjson.JSONObject;
import com.liuhu.socket.common.DateUtils;
import com.liuhu.socket.common.MathConstants;
import com.liuhu.socket.common.ResponseResult;
import com.liuhu.socket.domain.MarketInputDomain;
import com.liuhu.socket.domain.MarketOutputDomain;
import com.liuhu.socket.domain.NetIncomeInputDomain;
import com.liuhu.socket.entity.MarketInfo;
import com.liuhu.socket.enums.TradeStatusEnum;
import com.liuhu.socket.service.SharesInfoService;
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
        double netIncome = MathConstants.Pointkeep((input.getSaleUnit() - input.getBuyUnit()) * input.getSaleNum() - buyCommission - saleCommission,2);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("netIncome",netIncome);
        jsonObject.put("saleCommission",saleCommission);
        jsonObject.put("buyCommission",buyCommission);
        return ResponseResult.done(jsonObject);
    }
}
