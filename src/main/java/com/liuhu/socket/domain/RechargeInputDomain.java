package com.liuhu.socket.domain;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Setter
@Getter
public class RechargeInputDomain implements Serializable {

	private static final long serialVersionUID = -923156671835410830L;
    /**
     * 充值金额
     */
	private double amount;
    /**
     * 用户id
     */
	private String personId;

}
