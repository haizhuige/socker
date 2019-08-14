package com.liuhu.socket.domain;

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
}
