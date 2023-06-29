package com.liuhu.socket.schedule;

import com.alibaba.fastjson.JSONArray;
import com.liuhu.socket.common.DateUtils;
import com.liuhu.socket.common.HttpClientUtils;
import com.liuhu.socket.common.MathConstants;
import com.liuhu.socket.dao.MarketInfoNewMapper;
import com.liuhu.socket.dao.ShareInfoMapper;
import com.liuhu.socket.domain.input.MarketInputDomain;
import com.liuhu.socket.dto.SockerExcelEntity;
import com.liuhu.socket.dto.SockerSouhuImportEntity;
import com.liuhu.socket.entity.MarketInfoNew;
import com.liuhu.socket.entity.ShareInfo;
import com.liuhu.socket.enums.SockerStatusEnum;
import com.liuhu.socket.service.SharesInfoService;
import com.liuhu.socket.service.impl.SharesInfoServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    @Value("${sohuDownload.url}")
    private String sohuUrl;
    @Value("${sohuDownload.param}")
    private String soHuStaticParam;



    ExecutorService executorService = Executors.newFixedThreadPool(10);

    @Resource
    MarketInfoNewMapper marketInfoNewMapper;

    @Scheduled(cron = "0 0 15 1/1 * ? ")
    public void getNewMarketInfo() throws IOException {
        /**
         * 查询关注的股票信息
         */
        ShareInfo shareInfo = new ShareInfo();
        shareInfo.setStatus(SockerStatusEnum.GROUNDING.getCode());
        List<ShareInfo> shareInfoList = shareInfoMapper.getShareInfo(shareInfo);
        for (ShareInfo excelShare : shareInfoList) {
            String shareCode = excelShare.getShareCode();
            Date date = sharesInfoService.queryMaxDate(excelShare.getShareCode());
            /**
             * 网易股票下载当天的行情数据
             */
            MarketInputDomain inputDomain = new MarketInputDomain();
            inputDomain.setShareCode(shareCode);
            if (date == null) {
                inputDomain.setStartTime(DateUtils.operateDate(new Date(), -600, DateUtils.DateFormat.YYYYMMDD.getFormat()));
            } else {
                inputDomain.setStartTime(DateUtils.operateDate(date, -2, DateUtils.DateFormat.YYYYMMDD.getFormat()));

            }
            inputDomain.setShareName(excelShare.getShareName());
            inputDomain.setEndTime(DateUtils.format(new Date(), DateUtils.DateFormat.YYYYMMDD));
            List<String> list = this.getInfoByParam(inputDomain);
            if (list != null) {
                List<SockerExcelEntity> excelList = sealEntity(list);
                if (excelList != null && excelList.size() > 0) {
                    sharesInfoService.insertOrUpdateMarketInfo(excelList);
                }
            }

        }
    }

    @Scheduled(cron = "0 0 15 1/1 * ? ")
    public void getMarketInfoBySouHu(String originShareCode) throws IOException {
        /**
         * 查询关注的股票信息
         */
        ShareInfo shareInfo = new ShareInfo();
        shareInfo.setStatus(SockerStatusEnum.GROUNDING.getCode());
        List<ShareInfo> shareInfoList = new ArrayList<>();
        if (StringUtils.isEmpty(originShareCode)){
            shareInfoList = shareInfoMapper.getShareInfo(shareInfo);
        }else {
            ShareInfo newShareInfo = new ShareInfo();
            newShareInfo.setShareCode(originShareCode);
            shareInfoList.add(newShareInfo);
        }
        for (ShareInfo excelShare : shareInfoList) {
          //  queryInsert(excelShare);
            PatchThread patchThread = new PatchThread(excelShare);
            executorService.execute(patchThread);

        }
    }

    private void queryInsert(ShareInfo excelShare) throws IOException {
        String shareCode = excelShare.getShareCode();
        Date date = marketInfoNewMapper.queryMaxDate(shareCode);


        //   executorService.execute();
        /**
         * 搜狐股票下载当天的行情数据
         */
        MarketInputDomain inputDomain = new MarketInputDomain();
        inputDomain.setShareCode(shareCode);
        if (date == null) {
            inputDomain.setStartTime(DateUtils.operateDate(new Date(), -3000, DateUtils.DateFormat.YYYYMMDD.getFormat()));
        } else {
            inputDomain.setStartTime(DateUtils.operateDate(date, 1, DateUtils.DateFormat.YYYYMMDD.getFormat()));

        }
        inputDomain.setEndTime(DateUtils.format(new Date(), DateUtils.DateFormat.YYYYMMDD));
        if (inputDomain.getEndTime().compareTo(inputDomain.getStartTime()) < 0) {
            return;
        }
        SockerSouhuImportEntity importEntity = this.getMarketJsonBySouhu(inputDomain);
        if (importEntity == null) {
            return;
        }
        List<MarketInfoNew> list = importEntity.getList();
        if (list != null && list.size() > 0) {
            marketInfoNewMapper.insertOrUpdateMarketInfo(list);
        }
    }

    private List<String> getInfoByParam(MarketInputDomain inputDomain) throws IOException {
        List<String> list = new ArrayList<>();
        //有的股票是前缀加0 有的股票前缀加1
        String[] array = {"0", "1"};
        for (int i = array.length - 1; i >= 0; i--) {
            if (list != null && list.size() > 1) {
                return new ArrayList<>();
            }
            // 参数
            StringBuffer params = new StringBuffer();
            // 字符数据最好encoding以下;这样一来，某些特殊字符才能传过去(如:某人的名字就是“&”,不encoding的话,传不过去)
            if ("A00001".equals(inputDomain.getShareCode())) {
                params.append("code=0000001");
            } else {
                params.append("code=" + array[i] + inputDomain.getShareCode());
            }
            params.append("&");
            params.append("start=" + inputDomain.getStartTime().replace("-", ""));
            params.append("&");
            params.append("end=" + inputDomain.getEndTime().replace("-", ""));
            // 创建Get请求
            String totalUrl = url + params + staticParam;
            list = HttpClientUtils.commonGetMethodByParam(totalUrl);
            if (list != null && list.size() >= 2 && list.get(1).contains(inputDomain.getShareName())) {
                if ("A00001".equals(inputDomain.getShareCode())) {
                    list.replaceAll(t -> t.replace("000001", "A00001"));
                }
                return list;
            } else {
                list = null;
            }
        }
        return list;
    }
    @Override
    public SockerSouhuImportEntity getMarketJsonBySouhu(MarketInputDomain inputDomain) throws IOException {
        JSONArray array;
        String period = inputDomain.getPeriod();
        String periodUrl;
        StringBuffer params = new StringBuffer();
        if ("A00001".equals(inputDomain.getShareCode())) {
            params.append("code=zs_000001");
        }else if ("399300".equals(inputDomain.getShareCode())){
            params.append("code=" + "zs_399300");
        }
        else {
            params.append("code=" + "cn_" + inputDomain.getShareCode());
        }
        params.append("&");
        params.append("start=" + inputDomain.getStartTime().replace("-", ""));
        params.append("&");
        params.append("end=" + inputDomain.getEndTime().replace("-", ""));
        // 创建Get请求
        if (StringUtils.isEmpty(period)){
            periodUrl = "&period=d";
        }else {
            periodUrl = "&period="+period;
        }
        String totalUrl = sohuUrl + params + soHuStaticParam+ periodUrl;
        String response = HttpClientUtils.getXpath(totalUrl);
        if ("{}".equals(response)){
            return null;
        }
        array =   JSONArray.parseArray(response);
        if (array == null) {
            return null;
        }
        SockerSouhuImportEntity entity = array.getObject(0, SockerSouhuImportEntity.class);
        List<List<String>> hqList = entity.getHq();
        logger.info(hqList);
        if (CollectionUtils.isEmpty(hqList)){
            return entity;
        }
        String code = entity.getCode();
        List<MarketInfoNew> sohuList = new ArrayList<>();
        for (List<String> list : hqList) {
            MarketInfoNew socker = new MarketInfoNew();
            socker.setDate(DateUtils.parse(list.get(0), DateUtils.DateFormat.YYYY_MM_DD));
            socker.setShareCode(code);
            socker.setOpenValue(MathConstants.ParseStrPointKeep(list.get(1), 2));
            socker.setEndValue(MathConstants.ParseStrPointKeep(list.get(2), 2));
            socker.setRiseFall(MathConstants.ParseStrPointKeep(list.get(3), 2));
            if (list.get(4).contains("%")) {
                socker.setRiseFallRatio(MathConstants.ParseStrPointKeep(list.get(4).replace("%", ""), 4));
            }
            socker.setRiseFallRatioStr(list.get(4));
            socker.setLowest(MathConstants.ParseStrPointKeep(list.get(5), 2));
            socker.setHighest(MathConstants.ParseStrPointKeep(list.get(6), 2));
            socker.setDealCount(Long.parseLong(list.get(7)));
            socker.setDealAmount(MathConstants.ParseStrPointKeep(list.get(8), 4) * 10000);
            if (list.get(9).contains("%")) {
                socker.setTurnOverRate(MathConstants.ParseStrPointKeep(list.get(9).replace("%", ""), 4));
            }
            sohuList.add(socker);
        }
        entity.setList(sohuList);
        return entity;
    }

    /**
     * 封装需要插入的对象
     *
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


    class PatchThread implements Runnable{

        ShareInfo excelShare;

        public void setExcelShare(ShareInfo excelShare) {
            this.excelShare = excelShare;
        }

        public ShareInfo getExcelShare() {
            return excelShare;
        }
        PatchThread(ShareInfo shareInfo){
            excelShare = shareInfo;
        }


        @Override
        public void run() {
            try {
                queryInsert(excelShare);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
