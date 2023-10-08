package com.liuhu.socket.domain.output;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CalNextSaleOrBuyOutDTO {

    private Double preHandlePrice;

    private String shareCode;

    private Integer preHandleCount;

    private Double finalHandleCount;

    //操作类型 s:卖   b:买
    private String finalHandleType;

}
