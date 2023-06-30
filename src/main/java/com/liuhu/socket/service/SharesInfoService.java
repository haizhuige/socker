package com.liuhu.socket.service;

import java.util.Date;
import java.util.List;

import com.liuhu.socket.domain.input.MarketInputDomain;
import com.liuhu.socket.domain.input.QueryRecentSerialRedConditionDTO;
import com.liuhu.socket.domain.output.MarketOutputDomain;
import com.liuhu.socket.domain.output.QueryRecentSerialRedOutPutDTO;
import com.liuhu.socket.dto.SockerExcelEntity;
import com.liuhu.socket.entity.MarketInfo;
import com.liuhu.socket.entity.ShareInfo;

public interface SharesInfoService {
	
	List<MarketInfo> getShareInfo(MarketInputDomain input);
    /**
     * 查询时间范围内的增长率
     * @param input
     * @return
     */
	List<MarketOutputDomain> getRiseOfRate(MarketInputDomain input);
    /**
     * 插入更新数据
     * @param excelList
     */
    void insertOrUpdateMarketInfo(List<SockerExcelEntity> excelList);

    /**
     * 获取最新的时间
     * @return
     */
    Date queryMaxDate(String shareCode);
    /**
     * 查询时间范围内的增长率
     * @param input
     * @return
     */
    List<MarketOutputDomain> getRiseOfRateBySohu(MarketInputDomain input);

    /**
     * 查询实时股票数据
     */
    List<MarketOutputDomain> getRealTimeRateByWangyi();

    /**
     * 查询以周、月为维度计算收益增长率
     * @param input
     * @return
     */
    List<MarketOutputDomain> getDimentionRate(MarketInputDomain input);
    /**
     * 获取股票代码及其名称
     * @return
     */
    List<ShareInfo> getShareInfo();

    /**
     * 查询连续吓得之后又连续增长的股票及其收益率
     * @param input2Domain
     * @return
     */
    List<QueryRecentSerialRedOutPutDTO> queryRecentSerialRed(QueryRecentSerialRedConditionDTO input2Domain) throws Exception;
}
