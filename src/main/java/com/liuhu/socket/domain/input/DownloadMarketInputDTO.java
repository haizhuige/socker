package com.liuhu.socket.domain.input;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
public class DownloadMarketInputDTO implements Serializable {

    private String originalShareCode;
    //类型  A：socker  B：fund
    private String shareCodeType;
}
