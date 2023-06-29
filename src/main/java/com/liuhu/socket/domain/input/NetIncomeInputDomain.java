package com.liuhu.socket.domain.input;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * 计算净收入入参
 */
@Setter
@Getter
public class NetIncomeInputDomain implements Serializable {

	private static final long serialVersionUID = -923156671835410830L;
	
	private double buyUnit;
	
	private double saleUnit;
	
	private int  saleNum;


}
