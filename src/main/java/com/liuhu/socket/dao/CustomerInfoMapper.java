package com.liuhu.socket.dao;

import com.liuhu.socket.entity.CustomerInfo;

public interface CustomerInfoMapper {
    int insert(CustomerInfo record);

    int insertSelective(CustomerInfo record);
}