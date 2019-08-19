package com.liuhu.socket.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
@Getter
@Setter
public class PersonalInfo implements Serializable {
    private Integer id;

    private String personId;

    private Double totalAmount;

    private Double totalShare;

    private Double totalMarketValue;

    private Date updateDate;

    private String status;

    private double commission;
    private static final long serialVersionUID = 1L;
}