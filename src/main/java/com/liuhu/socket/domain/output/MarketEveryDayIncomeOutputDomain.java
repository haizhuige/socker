package com.liuhu.socket.domain.output;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
public class MarketEveryDayIncomeOutputDomain implements Serializable {

	/**
	 *总金额
	 */
	private double sumAmount;
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
	 * 一手股票初始价格
	 */
    private double initPrice;
	/**
	 * 一手股票尾盘价格
	 */
	private double endPrice;
    /**
     * 大盘变化率
     */
    private String ARate;



}
