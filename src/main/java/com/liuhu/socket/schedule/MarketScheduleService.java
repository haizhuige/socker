package com.liuhu.socket.schedule;

import com.liuhu.socket.domain.input.DownloadMarketInputDTO;
import com.liuhu.socket.domain.input.MarketInputDomain;
import com.liuhu.socket.dto.SockerSouhuImportEntity;

import java.io.IOException;

public interface MarketScheduleService {

     void  getNewMarketInfo() throws IOException;

     SockerSouhuImportEntity getMarketJsonBySouhu(MarketInputDomain inputDomain) throws IOException;

     /**
      * 带重试机制的搜狐行情获取方法
      * @param inputDomain 输入参数
      * @param maxRetries 最大重试次数
      * @return 行情数据实体
      */
     SockerSouhuImportEntity getMarketJsonBySouhuWithRetry(MarketInputDomain inputDomain, int maxRetries) throws Exception;

    void runMarketDataInfo(DownloadMarketInputDTO downloadMarketInputDTO);
}
