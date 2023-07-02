package com.liuhu.socket.dao;

import com.liuhu.socket.domain.output.QueryRecentSerialRedOutPutDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SerialTempMapper {

    int insertList(@Param("list") List<QueryRecentSerialRedOutPutDTO> list);

}