package com.liuhu.socket.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
public class ConditionShareCodeInfo {

    private Long id;

    private String shareCode;

    private String type;

    private Date date;

    private Double allProfit = 0.0;

    private  Double tFinalRatio = 0.0;

    private  Double methodRunRatio = 0.0;

    private Boolean isFinish = false;

    private Date endDate;

    private Integer doubleSize=1;

    private Double unitProfit=0.0;

    private Integer k = 0;

    private Double sumRatio;

    private Integer countDay;

    private Double score;

    private Date startDate;

    private String shareName;
}
