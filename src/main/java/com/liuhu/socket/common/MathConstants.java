package com.liuhu.socket.common;

import com.liuhu.socket.service.impl.SharesInfoServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;

public class MathConstants {
    private static final Logger logger = LogManager.getLogger(SharesInfoServiceImpl.class);

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
}
