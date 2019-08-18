package com.liuhu.socket.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
@Setter
@Getter
public class PersonalDetailInfo implements Serializable {
    private Integer id;

    private String personId;

    private String shareCode;

    private Double tradePrice;

    private Double currentPrice;

    private Double handPrice;

    private Integer handNum;

    private Integer holdDay;

    private String sharePer;

    private Double shareAmount;

    private Date updateDate;

    private String status;

    private static final long serialVersionUID = 1L;
}