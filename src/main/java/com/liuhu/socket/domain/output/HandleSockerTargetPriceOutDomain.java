package com.liuhu.socket.domain.output;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class HandleSockerTargetPriceOutDomain {

    private String socketCode;

    private String socketName;

    private Double handlePrice;

    //操作类型  1:买入   2：卖出
    private String handleType;


}
