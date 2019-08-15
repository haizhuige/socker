package com.liuhu.socket.service.impl;

import com.liuhu.socket.common.MathConstants;
import com.liuhu.socket.dao.CustomerInfoMapper;
import com.liuhu.socket.dao.RechargeInfoMapper;
import com.liuhu.socket.domain.CustomerInputDomain;
import com.liuhu.socket.domain.RechargeInputDomain;
import com.liuhu.socket.entity.CustomerInfo;
import com.liuhu.socket.entity.RechargeInfo;
import com.liuhu.socket.service.RechargeInfoService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @Author liuhu-jk
 * @Date 2019/8/15 17:46
 * @Description
 **/
@Service
public class RechargeInfoServiceImpl implements RechargeInfoService {
    @Resource
    CustomerInfoMapper customerInfoMapper;
    @Resource
    RechargeInfoMapper rechargeInfoMapper;
    @Override
    public Integer addCustomer(CustomerInputDomain input) {
        CustomerInfo customerInfo = new CustomerInfo();
        BeanUtils.copyProperties(input,customerInfo);
        customerInfo.setPersonId(MathConstants.generateCode("Cn",5));
        customerInfo.setUpdateDate(new Date());
        return  customerInfoMapper.insertSelective(customerInfo);
    }

    @Override
    public Integer operateTrade(RechargeInputDomain input) {
        RechargeInfo rechargeInfo = new RechargeInfo();
        BeanUtils.copyProperties(input,rechargeInfo);
        rechargeInfo.setUpdateDate(new Date());
        int i =  rechargeInfoMapper.insertSelective(rechargeInfo);
        if(i>0){

        }
        return null;
    }
}
