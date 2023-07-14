package com.liuhu.socket.domain.output;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class MarketOutputDomain implements Serializable {

	private static final long serialVersionUID = -923156671835410830L;
	/**
	 * 股票代码
	 */
	private String shareCode;
	/**
	 * 股票名称
	 */
	private String shareName;
	/**
	 * 时间范围内的收益率
	 */
	private double rate;
	/**
	 * 每天定投单位时间内的收益率
	 */
	private double fixRate;
	/**
	 * 大盘变化率
	 */
	private String ARate;

	private String rateStr;

	private String startTime;

	private String endTime;

	private double turnOverRate;


	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		MarketOutputDomain person = (MarketOutputDomain) o;
		return shareCode == person.shareCode &&
				shareCode.equals(person.shareCode);
	}

	@Override
	public int hashCode() {
		return shareCode.hashCode();
	}
}
