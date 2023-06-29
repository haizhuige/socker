package com.liuhu.socket.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
@Setter
@Getter
public class TradeInfo implements Serializable {
    private Integer id;

    private String personId;

    private String shareCode;

    private Double tradePrice;

    private Double handNum;

    private Date tradeDate;

    private String type;

    private Date updateDate;

    private String status;

    private static final long serialVersionUID = 1L;

}