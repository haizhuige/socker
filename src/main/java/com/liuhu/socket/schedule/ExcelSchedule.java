package com.liuhu.socket.schedule;

import com.liuhu.socket.common.ExcelUtils;
import com.liuhu.socket.common.HttpClientUtils;
import com.liuhu.socket.common.MathConstants;
import com.liuhu.socket.dao.ShareInfoMapper;
import com.liuhu.socket.domain.MarketInputDomain;
import com.liuhu.socket.dto.SockerExcelEntity;
import com.liuhu.socket.entity.ShareInfo;
import com.liuhu.socket.enums.SockerStatusEnum;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Component("excelTask")
public class ExcelSchedule {
    @Resource
    ShareInfoMapper shareInfoMapper;
    @Value("download.url")
    private String url;
    @Value("download.param")
    private String staticParam;
    @Scheduled(cron = "0 0 15 1/1 * ? ")
    public void  getNewMarketInfo(){
        ShareInfo shareInfo = new ShareInfo();
        shareInfo.setStatus(SockerStatusEnum.GROUNDING.getCode());
        List<ShareInfo> shareInfoList = shareInfoMapper.getShareInfo(shareInfo);
        for (ShareInfo excelShare:shareInfoList){
             String shareCode = excelShare.getShareCode();
             String file =  "C:\\Users\\Administrator\\Downloads\\"+shareCode+".csv";
             List<String> list =  ExcelUtils.importCsv(file);
             List<SockerExcelEntity> excelList = new ArrayList<>();
             for(int i = 1;i<list.size();i++){
                String splitStr = list.get(i);
                String split[] =splitStr.split(",");
                SockerExcelEntity socker = new SockerExcelEntity();
                socker.setDate(split[0]);
                socker.setShareCode(split[1]);
                socker.setShareName(split[2]);
                socker.setEndValue(MathConstants.ParseStrPointKeep(split[3], 4));
                socker.setHighest(MathConstants.ParseStrPointKeep(split[4], 4));
                socker.setLowest(MathConstants.ParseStrPointKeep(split[5], 4));
                socker.setOpenValue(MathConstants.ParseStrPointKeep(split[6], 4));
                socker.setPreEndValue(MathConstants.ParseStrPointKeep(split[7], 4));
                socker.setRiseFall(MathConstants.ParseStrPointKeep(split[8], 4));
                socker.setRiseFallRatio(MathConstants.ParseStrPointKeep(split[9], 4));
                excelList.add(socker);
             }
        }
    }
    private  void GetInfoByParam(MarketInputDomain inputDomain) {
        // 参数
        StringBuffer params = new StringBuffer();
        // 字符数据最好encoding以下;这样一来，某些特殊字符才能传过去(如:某人的名字就是“&”,不encoding的话,传不过去)
        params.append("code=" + inputDomain.getShareCode());
        params.append("&");
        params.append("start=" + inputDomain.getStartTime().replace("-", ""));
        params.append("&");
        params.append("end=" + inputDomain.getEndTime().replace("-", ""));
        // 创建Get请求
        String totalUrl = url + params + staticParam;
        HttpClientUtils.commonGetMethodByParam(totalUrl);
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
