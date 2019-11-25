package com.liuhu.socket.dao;

import java.util.List;

import com.liuhu.socket.entity.ShareInfo;

public interface ShareInfoMapper {
    int insert(ShareInfo record);

    int insertSelective(ShareInfo record);
    
    List<ShareInfo> getShareInfo(ShareInfo record);

    List<ShareInfo> getShareInfoWithoutASocker(ShareInfo shareInfo);

    String getRealTimeRateByWangyi(ShareInfo shareInfo);
}