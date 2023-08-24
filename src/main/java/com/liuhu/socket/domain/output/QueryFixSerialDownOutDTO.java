package com.liuhu.socket.domain.output;

import lombok.Data;

@Data
public class QueryFixSerialDownOutDTO {

    private String shareCode;

    private String shareName;
    //查询区间收益率
    private Double sumRatio;
    //etf类型
    private String type;
}
