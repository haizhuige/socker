package com.liuhu.socket.domain;

import lombok.Getter;
import lombok.Setter;

/**
 * @Author liuhu-jk
 * @Date 2019/8/15 17:57
 * @Description 客户管理输入参数
 **/
@Setter
@Getter
public class CustomerInputDomain {

    private String personName;

    private String phoneNo;

    private String IdNumber;
}
