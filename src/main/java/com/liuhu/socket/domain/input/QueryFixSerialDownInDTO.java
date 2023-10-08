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

    //最大连续次数
    private Integer maxSerialCount = 10;

    //最小连续次数
    private Integer minSerialCount = 3;

    //最小的连续水下次数
    private Integer minDownRiver = 2;
    //最小的连续水下次数
    private Double minSumRatio = -1.0;
}
