package com.liuhu.socket.service.impl;

import com.liuhu.socket.common.MathConstants;
import com.liuhu.socket.dao.CustomerInfoMapper;
import com.liuhu.socket.dao.PersonalInfoMapper;
import com.liuhu.socket.dao.RechargeInfoMapper;
import com.liuhu.socket.domain.input.CustomerInputDomain;
import com.liuhu.socket.domain.input.RechargeInputDomain;
import com.liuhu.socket.entity.CustomerInfo;
import com.liuhu.socket.entity.PersonalInfo;
import com.liuhu.socket.entity.RechargeInfo;
import com.liuhu.socket.service.RechargeInfoService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

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
    @Resource
    PersonalInfoMapper personalInfoMapper;
    @Override
    public Integer addCustomer(CustomerInputDomain input) {
        CustomerInfo customerInfo = new CustomerInfo();
        BeanUtils.copyProperties(input,customerInfo);
        customerInfo.setPersonId(MathConstants.generateCode("Cn",5));
        customerInfo.setUpdateDate(new Date());
        return  customerInfoMapper.insertSelective(customerInfo);
    }

    @Override
    @Transactional
    public Integer addRecharge(RechargeInputDomain input) {
        RechargeInfo rechargeInfo = new RechargeInfo();
        BeanUtils.copyProperties(input,rechargeInfo);
        rechargeInfo.setUpdateDate(new Date());
        String personId = input.getPersonId();
        double amount = input.getAmount();
        rechargeInfo.setRecharge(amount);
        int updateNum =0;
        int i =  rechargeInfoMapper.insertSelective(rechargeInfo);
        if(i>0){
            PersonalInfo personalInfo = new PersonalInfo();
            personalInfo.setPersonId(input.getPersonId());
            personalInfo.setUpdateDate(new Date());
            personalInfo.setTotalAmount(amount);
            List<PersonalInfo> list = personalInfoMapper.queryByEntity(personalInfo);
            if(list.size()>0){
                double originTotalAmount = list.get(0).getTotalAmount();
                personalInfo.setTotalAmount(amount+originTotalAmount);
                updateNum = personalInfoMapper.updateAmountByPersonId(personalInfo);
            }else{
                updateNum =personalInfoMapper.insertSelective(personalInfo);
            }
        }
        return updateNum;
    }
}
