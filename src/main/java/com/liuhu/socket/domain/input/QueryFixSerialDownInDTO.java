package com.liuhu.socket.domain.input;

import lombok.Data;

import java.util.Date;

@Data
public class QueryFixSerialDownInDTO {

    private Date startDate;

    private Date endDate;

    private String startDateStr;
    //连续下跌数量
    private Integer serialDownCount;

    //固定下降额度
    private Double fixDownRatio=-5.0;
}
