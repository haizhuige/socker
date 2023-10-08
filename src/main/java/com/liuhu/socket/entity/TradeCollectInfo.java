package com.liuhu.socket.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Setter
@Getter
public class TradeCollectInfo implements Serializable {

    private static final long serialVersionUID = 1L;


    private Long id;

    private String shareCode;

    private String shareName;

    private Date buyDate;

    private Date saleDate;

    private Double buyPrice;

    private Double preSPrice;

    private Double realSPrice;


    private Integer buyCount;

    private String isAble;

    //详情见PlanEnum 买入方案类型
    private String type;

    private Integer doubleSize;

    private Double sumRatio;



}