package com.liuhu.socket.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
public class DetailInfoBuyTypeBuyDTO {


    private String shareCode;

    private String type;

    private Date date;

    private Date endDate;

    private Integer doubleSize=1;

    private Double unitProfit=0.0;
}
