package com.liuhu.socket.domain.output;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.Map;

@Setter
@Getter
public class RecentDownOrderOutPutDTO {


    private Integer doubleSize;

    private String shareCode;

    private Double finalProfit;

    private Double runRatio;


}
