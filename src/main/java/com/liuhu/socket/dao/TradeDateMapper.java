package com.liuhu.socket.dao;

import com.liuhu.socket.entity.TradeDateInfo;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface TradeDateMapper {
    int insert(TradeDateInfo record);

    Date queryMaxDate();

    int insertList(List<Date> insertDateList);

    Date getWantDate(@Param("recentDay") Integer recentDay,@Param("date") Date date,@Param("type")String type);

    List<Date> queryPeriodDateList(@Param("date") String endTime,@Param("recentDay") Integer period);


}