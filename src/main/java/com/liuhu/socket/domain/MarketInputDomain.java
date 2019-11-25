package com.liuhu.socket.domain;

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
@Setter
@Getter
public class MarketInputDomain implements Serializable {

	private static final long serialVersionUID = -923156671835410830L;
	
	private String startTime;
	
	private String endTime;
	
	private String shareCode;

    private Date startTimeDa;

    private Date endTimeDa;

    private String shareName;

    private String period;

}
