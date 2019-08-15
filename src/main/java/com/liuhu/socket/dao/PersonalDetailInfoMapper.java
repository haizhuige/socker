package com.liuhu.socket.dao;

import com.liuhu.socket.entity.PersonalDetailInfo;

public interface PersonalDetailInfoMapper {
    int insert(PersonalDetailInfo record);

    int insertSelective(PersonalDetailInfo record);
}