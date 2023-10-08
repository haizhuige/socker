package com.liuhu.socket.dao;

import com.liuhu.socket.domain.input.QueryProfitByComProgram;
import com.liuhu.socket.entity.ConditionShareCodeInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ConditionShareInfoMapper {

    void insertList(@Param(value = "list") List<ConditionShareCodeInfo> shareCodeInfoList);

    List<ConditionShareCodeInfo> queryShareCodeByCondition(@Param(value = "condition")QueryProfitByComProgram queryProfitByComProgram);
}
