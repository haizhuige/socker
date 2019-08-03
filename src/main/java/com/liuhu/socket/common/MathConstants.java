package com.liuhu.socket.common;

import java.math.BigDecimal;

public class MathConstants {

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
		BigDecimal big = new BigDecimal(param);
		return big.setScale(point, BigDecimal.ROUND_HALF_UP).doubleValue();
	}
}
