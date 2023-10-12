package com.liuhu.socket.domain.input;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

import java.util.List;

@Setter
@Getter
public class DownloadMarketInputDTO implements Serializable {

    private String originalShareCode;
    //类型  A：socker  B：fund
    private String shareCodeType;

    private String startTime;

    private String endTime;

    private List<String> planList;
}
