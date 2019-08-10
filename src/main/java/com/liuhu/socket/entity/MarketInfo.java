package com.liuhu.socket.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
@Setter
@Getter
public class MarketInfo implements Serializable {
    private Integer id;
    
    private String shareCode;

    private Double openValue;

    private Double endValue;

    private Double riseFall;

    private String riseFallRatio;

    private Double highest;

    private Double lowest;

    private Date date;
    
    private Double totalAmount;

    private Double preEndValue;

    private String shareName;
    private static final long serialVersionUID = 1L;
}