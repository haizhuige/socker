package com.liuhu.socket.service;

import java.util.Date;
import java.util.List;

import com.liuhu.socket.domain.MarketInputDomain;
import com.liuhu.socket.domain.MarketOutputDomain;
import com.liuhu.socket.dto.SockerExcelEntity;
import com.liuhu.socket.entity.MarketInfo;

public interface SharesInfoService {
	
	List<MarketInfo> getShareInfo(MarketInputDomain input);
    /**
     * 查询时间范围内的增长率
     * @param input
     * @return
     */
	List<MarketOutputDomain> getRiseOfRate(MarketInputDomain input);
	/**
	 * 查询每天都买一手的收益
	 * @param input
	 * @return
	 */
	List<MarketOutputDomain> getBuyEveryDay(MarketInputDomain input);

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
}
