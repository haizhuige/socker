package com.liuhu.socket.domain.input;

import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
public class QueryProfitByComProgram {

    private String startTime;

    private String endTime;

    private List<String> planList;

    private Date startDate;

    //持有数量
    private Map<Date,List<String>> resultMap;


    private Integer  holdCount=0;

    private String plan;




}
