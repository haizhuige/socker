package com.liuhu.socket.domain;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Setter
@Getter
public class MarketInput2Domain implements Serializable {

	private static final long serialVersionUID = -923156671835410830L;
	
	private String startTime;
	
	private String endTime;
	
	private String shareCode;

    private Date startTimeDa;

    private Date endTimeDa;

    private String shareName;

}
