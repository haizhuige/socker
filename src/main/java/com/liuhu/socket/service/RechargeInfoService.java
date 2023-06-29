package com.liuhu.socket.service;

import com.liuhu.socket.domain.input.CustomerInputDomain;
import com.liuhu.socket.domain.input.RechargeInputDomain;

public interface RechargeInfoService {


    Integer addCustomer(CustomerInputDomain input);

    Integer addRecharge(RechargeInputDomain input);
}
