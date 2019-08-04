package com.liuhu.socket.schedule;

import com.liuhu.socket.dao.ShareInfoMapper;
import com.liuhu.socket.entity.ShareInfo;
import com.liuhu.socket.enums.SockerStatusEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component("excelTask")
public class ExcelSchedule {
    @Resource
    ShareInfoMapper shareInfoMapper;
    @Value("download.url")
    private String url;
    @Value("download.param")
    private String param;
    @Scheduled(cron = "0 0 15 1/1 * ? ")
    public void  getNewMarketInfo(){
        ShareInfo shareInfo = new ShareInfo();
        shareInfo.setStatus(SockerStatusEnum.GROUNDING.getCode());
        List<ShareInfo> shareInfoList = shareInfoMapper.getShareInfo(shareInfo);
        for (ShareInfo excelShare:shareInfoList){
             String shareCode = excelShare.getShareCode();
             String file =  "C:\\Users\\Administrator\\Downloads\\"+shareCode+".csv";
             
        }
    }
}
