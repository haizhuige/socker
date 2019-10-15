package com.liuhu.socket.schedule;

import com.liuhu.socket.domain.MarketInputDomain;
import com.liuhu.socket.dto.SockerSouhuImportEntity;

import java.io.IOException;

public interface MarketScheduleService {

    public void  getNewMarketInfo() throws IOException;

    public SockerSouhuImportEntity getMarketJsonBySouhu(MarketInputDomain inputDomain) throws IOException;

}
