package com.liuhu.socket.dao;

import com.liuhu.socket.entity.PersonalInfo;

public interface PersonalInfoMapper {
    int insert(PersonalInfo record);

    int insertSelective(PersonalInfo record);
}