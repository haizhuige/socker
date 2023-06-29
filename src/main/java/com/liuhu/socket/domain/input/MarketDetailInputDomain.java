package com.liuhu.socket.domain.input;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 查看每个股票收益详情
 */
@Setter
@Getter
public class MarketDetailInputDomain implements Serializable {

    private static final long serialVersionUID = -923156671835410830L;

    /**
     * 股票代码集
     */
    private List<String> shareCodeList;


    private Date startTimeDa;


    private Date endTimeDa;

    /**
     *最多购买股票支数
     */
    private Integer maxCount;

    /**
     * 投入资金量
     */
    private Double sumCount;
    /**
     *
     * 单只股票的最高价格
     */
    private Double maxUnitPrice;







}
