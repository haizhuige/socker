package com.liuhu.socket.domain.input;

import lombok.Data;

import java.util.Date;

@Data
public class QueryFixSerialDownInDTO {

    private Date startDate;

    private Date endDate;
    //连续下跌数量
    private Integer serialDownCount;
}
