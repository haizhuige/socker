package com.liuhu.socket.domain.output;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class QueryRecentSerialRedOutPutDTO {

    private String shareCode;
    //获取最大收益
    private Double maxRatio;
    //获取最少收益
    private Double minRatio;
    //最终收益
    private Double finalRatio;
    //开始买入起点日期
    private Date startTime;

    private Long id;

    //上涨区间 达到最小红的数量
    private Integer upPeriodCount;

    private Double downSumRatio;
}
