package com.liuhu.socket.dao;

import com.liuhu.socket.entity.PersonalDetailInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PersonalDetailInfoMapper {
    int insert(PersonalDetailInfo record);

    int insertSelective(PersonalDetailInfo record);

    List<PersonalDetailInfo> queryDetailInfo( PersonalDetailInfo detailInfo);

    int updatePersonalDetailByEntity(PersonalDetailInfo detailInfo);
}