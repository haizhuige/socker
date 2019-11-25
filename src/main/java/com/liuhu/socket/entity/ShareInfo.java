package com.liuhu.socket.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
@Setter
@Getter
public class ShareInfo implements Serializable {
    private String shareCode;

    private String shareName;

    private String status;

    private String hushenStatus;

    private static final long serialVersionUID = 1L;


}