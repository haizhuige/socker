package com.liuhu.socket.domain.input;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Setter
@Getter
public class CalNextSaleOrBuyInputDTO implements Serializable {

    private Double costPrice;

    private Integer CurrentDoubleSize;

    private Double tFinalRatio;

    private Integer holdCount;

    private String shareCode;

    private String startTime;

    private Double currentFinalRatio;

}
