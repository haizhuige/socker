package com.liuhu.socket.domain;

import com.liuhu.socket.common.annotation.NotNull;
import com.liuhu.socket.common.annotation.Validator;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Setter
@Getter
@Validator
public class TradeInputDomain implements Serializable {

	private static final long serialVersionUID = -923156671835410830L;
    /**
     * 投资人Id
     */
    @NotNull
	private String personId;
    /**
     * 投资时间
     */
    @NotNull
	private String investTime;
    /**
     * 投资股票代码
     */
    @NotNull
	private String shareCode;
    /**
     * 投资时每股价格
     */
    @NotNull
    private double unitValue;
    /**
     * 购买股数/赎回股票
     */
    @NotNull
    private Integer handNum;

}
