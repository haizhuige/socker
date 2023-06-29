package com.liuhu.socket.dao;

import com.liuhu.socket.entity.RechargeInfo;

public interface RechargeInfoMapper {
    int insert(RechargeInfo record);

    int insertSelective(RechargeInfo record);
}