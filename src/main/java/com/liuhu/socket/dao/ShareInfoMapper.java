package com.liuhu.socket.dao;

import java.util.List;

import com.liuhu.socket.entity.ShareInfo;
import org.apache.ibatis.annotations.Param;

public interface ShareInfoMapper {
    int insert(ShareInfo record);

    int insertSelective(ShareInfo record);
    
    List<ShareInfo> getShareInfo(ShareInfo record);

    List<ShareInfo> getShareInfoWithoutASocker(ShareInfo shareInfo);

    String getRealTimeRateByWangyi(ShareInfo shareInfo);
    //获取随机股票代码
    List<ShareInfo> getRandomSocket(@Param("count") Integer count);
}