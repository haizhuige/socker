package com.liuhu.socket.dao;

import com.liuhu.socket.entity.PersonalInfo;

import java.util.List;

public interface PersonalInfoMapper {
    int insert(PersonalInfo record);

    int insertSelective(PersonalInfo record);

    List<PersonalInfo> queryByEntity(PersonalInfo personalInfo);

    int updateAmountByPersonId(PersonalInfo personId);
}