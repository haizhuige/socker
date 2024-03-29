package com.liuhu.socket.common;

import com.liuhu.socket.enums.TradeStatusEnum;
import com.liuhu.socket.service.impl.SharesInfoServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;

public class MathConstants {
    private static final Logger logger = LogManager.getLogger(SharesInfoServiceImpl.class);
    //过户费率
    private static  double  transferPerFee = 0.0002;
    //成交费率
    private static  double  dealPerFee = 0.0000487;
    //佣金费率
    private static  double  commissionPerFee = 0.0003;
    //印花费率
    private static  double  stampTaxPerFee = 0.001;
    public static final String TEXT = "0123456789";

    public static String generateCode(String codePrefix, int length) {
        StringBuilder sb = new StringBuilder(codePrefix);
        sb.append(DateUtils.format(new Date(),DateUtils.DateFormat.YYYYMMDDHHMMSS));
        ThreadLocalRandom r = ThreadLocalRandom.current();
        for (int i = 0; i < length; i++) {
            sb.append(TEXT.charAt(r.nextInt(TEXT.length())));
        }
        return sb.toString();
    }
	public static  double Pointkeep(double param,int point) {
		BigDecimal big = new BigDecimal(param);
		return big.setScale(point, BigDecimal.ROUND_HALF_UP).doubleValue();
	}

	/**
	 * String 类型转换成固定小数位double类型
	 * @param param
	 * @param point
	 * @return
	 */
	public static  double ParseStrPointKeep(String param,int point) {
	    if(StringUtils.isEmpty(param)||"None".equals(param)){
	        return -100000;
        }
        try {
            BigDecimal big = new BigDecimal(param);
            return big.setScale(point, BigDecimal.ROUND_HALF_UP).doubleValue();
        } catch (Exception e) {
            logger.error("保留两位小数失败，param{},e{}",param,e);
            return 0.0;
        }
    }
    /**
     *股票买卖手续费计算
     */
    public static  double computerCommission(double tradeValue,int handNum,int type) {
        double commission =0;
        double transferFee =  handNum * transferPerFee;
        double dealFee =handNum*tradeValue * dealPerFee;
        double commissionFee = handNum*tradeValue *commissionPerFee;
       if (TradeStatusEnum.BUY.getCode().equals(type)){
           commission  = transferFee + dealFee+commissionFee;
           if (commission<5){
               return 5;
           }
         return Pointkeep(commission,2);
       }else if(TradeStatusEnum.SALE.getCode().equals(type)){
           double stampTaxFee =handNum*tradeValue*stampTaxPerFee;
           commission = stampTaxFee+transferFee + dealFee+commissionFee;
           if(commission<5){
               return 5;
           }
           return Pointkeep(stampTaxFee+transferFee + dealFee+commissionFee,2);
       }
        return 0.0;
    }
}
