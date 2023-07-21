package com.liuhu.socket.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Setter
@Getter
public class Xia2Shang1InnerDTO implements Serializable {
    //起始日期
    private Date startTime;

    //结束日期
    private Date endTime;

    //代码
    private String shareCode;

    //收益
    private Double income;

    //购买倍数
    private int purchaseUnitCount;

}
