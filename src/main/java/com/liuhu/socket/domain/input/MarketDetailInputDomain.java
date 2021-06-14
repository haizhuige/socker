package com.liuhu.socket.domain.input;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Setter
@Getter
public class MarketDetailInputDomain implements Serializable {

    private static final long serialVersionUID = -923156671835410830L;


    private List<String> shareCode;


    private Date startTimeDa;


    private Date endTimeDa;


    private Integer count;





}
