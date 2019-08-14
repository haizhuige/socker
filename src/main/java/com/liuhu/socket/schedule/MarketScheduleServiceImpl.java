package com.liuhu.socket.schedule;

import com.liuhu.socket.common.DateUtils;
import com.liuhu.socket.common.HttpClientUtils;
import com.liuhu.socket.common.MathConstants;
import com.liuhu.socket.dao.ShareInfoMapper;
import com.liuhu.socket.domain.MarketInputDomain;
import com.liuhu.socket.dto.SockerExcelEntity;
import com.liuhu.socket.entity.ShareInfo;
import com.liuhu.socket.enums.SockerStatusEnum;
import com.liuhu.socket.service.SharesInfoService;
import com.liuhu.socket.service.impl.SharesInfoServiceImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component("scheduleTask")
public class MarketScheduleServiceImpl implements MarketScheduleService {
    private static final Logger logger = LogManager.getLogger(SharesInfoServiceImpl.class);
    @Resource
    ShareInfoMapper shareInfoMapper;
    @Resource
    SharesInfoService sharesInfoService;
    @Value("${download.url}")
    private String url;
    @Value("${download.param}")
    private String staticParam;

    @Scheduled(cron = "0 0 15 1/1 * ? ")
    //@Scheduled(cron = "0 0/5 * * * ? ")
    public void getNewMarketInfo() throws IOException {
        /**
         * 查询关注的股票信息
         */
        ShareInfo shareInfo = new ShareInfo();
        shareInfo.setStatus(SockerStatusEnum.GROUNDING.getCode());
        List<ShareInfo> shareInfoList = shareInfoMapper.getShareInfo(shareInfo);
        for (ShareInfo excelShare : shareInfoList) {
            String shareCode = excelShare.getShareCode();
            Date date = sharesInfoService.queryMaxDate();
            /**
             * 网易股票下载当天的行情数据
             */
            MarketInputDomain inputDomain = new MarketInputDomain();
            inputDomain.setShareCode(shareCode);
            if(date ==null){
                inputDomain.setStartTime(DateUtils.operateDate(new Date(), -600, DateUtils.DateFormat.YYYYMMDD.getFormat()));
            }else{
                inputDomain.setStartTime(DateUtils.operateDate(date, 1, DateUtils.DateFormat.YYYYMMDD.getFormat()));

            }
            inputDomain.setEndTime(DateUtils.format(new Date(), DateUtils.DateFormat.YYYYMMDD));
            List<String> list = this.getInfoByParam(inputDomain);
            List<SockerExcelEntity> excelList = sealEntity(list);
            if (excelList != null && excelList.size() > 0) {
                sharesInfoService.insertOrUpdateMarketInfo(excelList);
            }
        }
    }

    private List<String> getInfoByParam(MarketInputDomain inputDomain) throws IOException {
        // 参数
        StringBuffer params = new StringBuffer();
        // 字符数据最好encoding以下;这样一来，某些特殊字符才能传过去(如:某人的名字就是“&”,不encoding的话,传不过去)
        params.append("code=" + "0" + inputDomain.getShareCode());
        params.append("&");
        params.append("start=" + inputDomain.getStartTime().replace("-", ""));
        params.append("&");
        params.append("end=" + inputDomain.getEndTime().replace("-", ""));
        // 创建Get请求
        String totalUrl = url + params + staticParam;
        List<String> list = HttpClientUtils.commonGetMethodByParam(totalUrl);
        return list;
    }

    /**
     * 封装需要插入的对象
     * @param list
     * @return
     */
    private static List<SockerExcelEntity> sealEntity(List<String> list) {
        List<SockerExcelEntity> excelList = new ArrayList<>();
        for (int i = 1; i < list.size(); i++) {
            String splitStr = list.get(i);
            String split[] = splitStr.split(",");
            SockerExcelEntity socker = new SockerExcelEntity();
            socker.setDate(split[0]);
            socker.setShareCode(split[1].replace("'", ""));
            socker.setShareName(split[2]);
            boolean flag = true;
            for (int j = 3; j < 10; j++) {
                if (-100000 == MathConstants.ParseStrPointKeep(split[j], 4)) {
                    flag = false;
                }
            }
            if (!flag) {
                break;
            }
            socker.setEndValue(MathConstants.ParseStrPointKeep(split[3], 4));
            socker.setHighest(MathConstants.ParseStrPointKeep(split[4], 4));
            socker.setLowest(MathConstants.ParseStrPointKeep(split[5], 4));
            socker.setOpenValue(MathConstants.ParseStrPointKeep(split[6], 4));
            socker.setPreEndValue(MathConstants.ParseStrPointKeep(split[7], 4));
            socker.setRiseFall(MathConstants.ParseStrPointKeep(split[8], 4));
            socker.setRiseFallRatio(MathConstants.ParseStrPointKeep(split[9], 4));
            if (split.length > 10) {
                socker.setTotalAmount(MathConstants.ParseStrPointKeep(split[10], 4));
            }
            excelList.add(socker);
        }
        return excelList;
    }
}
