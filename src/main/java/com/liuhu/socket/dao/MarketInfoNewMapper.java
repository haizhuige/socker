package com.liuhu.socket.dao;

import com.liuhu.socket.domain.input.MarketDetailInputDomain;
import com.liuhu.socket.domain.input.MarketInput2Domain;
import com.liuhu.socket.domain.input.QueryRecentSerialRedConditionDTO;
import com.liuhu.socket.domain.output.MarketOutputDomain;
import com.liuhu.socket.domain.output.QueryRecentSerialRedOutPutDTO;
import com.liuhu.socket.dto.QueryRecentSerialRedConditionDO;
import com.liuhu.socket.entity.MarketInfoNew;
import com.liuhu.socket.entity.ShareInfo;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface MarketInfoNewMapper {
    int insert(MarketInfoNew record);

    int insertSelective(MarketInfoNew record);

    void insertOrUpdateMarketInfo(List<MarketInfoNew> list);

    Date queryMaxDate(@Param("shareCode") String shareCode);

    List<Map> queryPeriodRateByShareCode(@Param("domain") MarketInput2Domain marketInput2Domain);

    List<MarketInfoNew> queryMarketInfoByParam(MarketDetailInputDomain input);

    List<Date> queryDistinctDate();

    List<String> queryMaxAmount();

    /**
     * 从下跌起始点开始输出计算v型结构后syl
     * @param input
     * @return
     */
    List<QueryRecentSerialRedOutPutDTO> queryVRatioFromDownStartPoint(QueryRecentSerialRedConditionDO input);

    /**
     * 查询 日期
     *
     */
<<<<<<< .merge_file_jKOYeo
    List<MarketInfoNew> queryMarketInfoByDate(@Param("date") Date newDate);

    /**
     * 查询满足连续n天上涨的代码
     *
     */
    List<MarketInfoNew> querySerialRedFiveInfoByDate(@Param("date") Date newDate,@Param("num")Integer num);
=======
    List<MarketInfoNew> queryMarketInfoByDate(@Param("condition") QueryRecentSerialRedConditionDTO condition);
>>>>>>> .merge_file_xUzqFY
    /**
     * 查询 三连down之后一天的收益
     *
     */
    List<QueryRecentSerialRedOutPutDTO> queryThreeDownThen(@Param("date")Date newDate, @Param("shareCodeList") List<String> shareCodeList);
}