package com.liuhu.socket.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
public class ConditionShareCodeInfo {

    private Long id;

    private String shareCode;

    private String type;

    private Date date;
}
