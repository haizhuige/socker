package com.liuhu.socket.domain.input;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 查询股票收益率入参 DTO
 * 用于计算股票收益率并存储到 serial_temp 表
 */
@Setter
@Getter
public class QueryStockReturnDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 股票代码（单股模式）
     */
    private String shareCode;

    /**
     * 是否全量模式（true=计算所有股票，false=计算单只股票）
     */
    private Boolean allFlag = false;

    /**
     * 查询起始时间（格式：yyyy-MM-dd HH:mm:ss）
     */
    private String startTime;

    /**
     * 查询结束时间（格式：yyyy-MM-dd HH:mm:ss）
     */
    private String endTime;

    /**
     * 沪深类型（B=沪深 A 股）
     */
    private String hushenStatus = "B";

    /**
     * 插入临时表时的 rateOrAmountDay 参数
     */
    private Integer rateOrAmountDay = 5;

    /**
     * 策略类型（用于选择具体的实现类）
     * 可选值：nextSerialRed（使用 TradeMethodByNextSerialRedServiceImpl）
     *        moRen（使用 TradeMethodServiceImpl）
     */
    private String methodType;

}
