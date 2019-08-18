package com.liuhu.socket.service;

import com.liuhu.socket.domain.CustomerInputDomain;
import com.liuhu.socket.domain.RechargeInputDomain;

public interface RechargeInfoService {


    Integer addCustomer(CustomerInputDomain input);

    Integer addRecharge(RechargeInputDomain input);
}
