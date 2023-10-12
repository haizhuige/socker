package com.liuhu.socket.schedule;

import com.liuhu.socket.domain.input.DownloadMarketInputDTO;
import com.liuhu.socket.domain.input.MarketInputDomain;
import com.liuhu.socket.dto.SockerSouhuImportEntity;

import java.io.IOException;

public interface MarketScheduleService {

     void  getNewMarketInfo() throws IOException;

     SockerSouhuImportEntity getMarketJsonBySouhu(MarketInputDomain inputDomain) throws IOException;

    void runMarketDataInfo(DownloadMarketInputDTO downloadMarketInputDTO);
}
