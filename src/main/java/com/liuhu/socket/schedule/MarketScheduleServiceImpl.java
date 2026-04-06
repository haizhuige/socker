package com.liuhu.socket.schedule;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.liuhu.socket.common.DateUtils;
import com.liuhu.socket.common.HttpClientUtils;
import com.liuhu.socket.common.MathConstants;
import com.liuhu.socket.dao.MarketInfoNewMapper;
import com.liuhu.socket.dao.ShareInfoMapper;
import com.liuhu.socket.dao.TradeDateMapper;
import com.liuhu.socket.domain.input.DownloadMarketInputDTO;
import com.liuhu.socket.domain.input.MarketInputDomain;
import com.liuhu.socket.domain.input.QueryProfitByComProgram;
import com.liuhu.socket.domain.input.QueryRecentSerialRedConditionDTO;
import com.liuhu.socket.domain.output.MarketRealTimeOutputDomain;
import com.liuhu.socket.dto.SockerExcelEntity;
import com.liuhu.socket.dto.SockerSouhuImportEntity;
import com.liuhu.socket.entity.MarketInfoNew;
import com.liuhu.socket.entity.ShareInfo;
import com.liuhu.socket.entity.TradeDateInfo;
import com.liuhu.socket.enums.SockerStatusEnum;
import com.liuhu.socket.service.SharesInfoService;
import com.liuhu.socket.service.TradeInfoService;
import com.liuhu.socket.service.TradeMethodService;
import com.liuhu.socket.service.impl.SharesInfoServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component("scheduleTask")
@Slf4j
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

    @Resource(name = "nextSerialRed")
    private TradeMethodService tradeMethodService;

    @Resource
    TradeInfoService tradeInfoService;

    @Resource
    MarketInfoNewMapper marketInfoNewMapper;

    @Resource
    TradeDateMapper tradeDateMapper;

    @Scheduled(cron = "0 0 15 1/1 * ? ")
    public void getNewMarketInfo() throws IOException {
        /**
         * 查询关注的股票信息
         */
        ShareInfo shareInfo = new ShareInfo();
        shareInfo.setStatus(SockerStatusEnum.GROUNDING.getCode());
        List<ShareInfo> shareInfoList = shareInfoMapper.getShareInfo(shareInfo);
        Date maxDate =null;
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
    public void getMarketInfoBySouHu(DownloadMarketInputDTO downloadMarketInputDTO) throws IOException {
        String originShareCode = downloadMarketInputDTO.getOriginalShareCode();
        /**
         * 查询关注的 socker 信息
         */
        ShareInfo shareInfo = new ShareInfo();
        shareInfo.setStatus(SockerStatusEnum.GROUNDING.getCode());
        if (Objects.nonNull(downloadMarketInputDTO.getShareCodeType())){
            shareInfo.setHushenStatus(downloadMarketInputDTO.getShareCodeType());
        }
        List<ShareInfo> shareInfoList = new ArrayList<>();
        if (StringUtils.isEmpty(originShareCode)){
           shareInfoList = shareInfoMapper.getShareInfo(shareInfo);
        }else {
            ShareInfo newShareInfo = new ShareInfo();
            newShareInfo.setShareCode(originShareCode);
            shareInfoList.add(newShareInfo);
        }

        int totalSize = shareInfoList.size();
        log.info("开始下载行情数据，共 {} 只股票", totalSize);

        // 根据股票数量动态调整线程池大小，最大 40 个线程（配合信号量限制）
        int threadPoolSize = Math.min(40, Math.max(20, totalSize / 125));
        ExecutorService executorService = Executors.newFixedThreadPool(threadPoolSize);
        CountDownLatch latch = new CountDownLatch(totalSize);

        log.info("线程池大小：{}，预计耗时：{} 分钟", threadPoolSize, (totalSize * 5 / threadPoolSize / 60) + 1);

        TradeDateInfo currentMaxDateInfo = tradeDateMapper.queryMaxDate();

        if (currentMaxDateInfo ==null){
            String s = DateUtils.operateDate(new Date(), -3000, DateUtils.DateFormat.YYYY_MM_DD_HH_MM_SS.getFormat());
            Date date = DateUtils.parse(s,DateUtils.DateFormat.YYYY_MM_DD_HH_MM_SS);
            currentMaxDateInfo = new TradeDateInfo();
            currentMaxDateInfo.setDate(date);
        }


        // 使用线程安全的失败股票列表
        final List<String> currentFailedShares = failedShareCodes;

        for (ShareInfo excelShare : shareInfoList) {
            PatchThread patchThread = new PatchThread(excelShare, currentFailedShares, latch);
            executorService.execute(patchThread);
        }

        // 关闭线程池（不再接受新任务）
        executorService.shutdown();

        // 等待所有任务完成
        try {
            // 动态计算超时时间：按每只股票平均 5 秒计算（含重试）
            // 5000 只股票 / 50 线程 * 5 秒 = 500 秒 ≈ 9 分钟
            long timeout = Math.max(600L, totalSize * 5L / threadPoolSize);
            log.info("等待任务完成，超时时间设置为 {} 秒（{} 分钟）", timeout, timeout / 60);
            if (!latch.await(timeout, TimeUnit.SECONDS)) {
                log.warn("部分股票下载任务未在规定时间内完成，剩余 {} 个任务", latch.getCount());
            } else {
                log.info("所有股票下载任务完成");
            }
        } catch (InterruptedException e) {
            log.error("等待任务完成时被中断", e);
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }

        // 插入缺失的日期到 trade_date 表
        List<Date> dateList = queryDistinctDate();
        Date currentMaxDate = currentMaxDateInfo.getDate();
        List<Date> insertDateList = dateList.stream().filter(date1 -> date1.compareTo(currentMaxDate) > 0).sorted(Date::compareTo).collect(Collectors.toList());
        List<TradeDateInfo> tradeDateInfoList = new ArrayList<>();
        int id = currentMaxDateInfo.getId();
        for (Date insertDate : insertDateList) {
            TradeDateInfo tradeDateInfo = new TradeDateInfo();
            tradeDateInfo.setDate(insertDate);
            tradeDateInfo.setId(++id);
            tradeDateInfoList.add(tradeDateInfo);
        }
        if (!tradeDateInfoList.isEmpty()) {
            tradeDateMapper.insertEntityList(tradeDateInfoList);
            log.info("插入 {} 个交易日日期到 trade_date 表", tradeDateInfoList.size());
        }

        // 输出失败统计
        if (!failedShareCodes.isEmpty()) {
            log.error("本次下载完成，共 {} 只股票失败，可以通过 /retryFailedShares.do 接口重试", failedShareCodes.size());
        } else {
            log.info("行情数据下载完成，全部成功！");
        }
    }

    @Transactional(propagation= Propagation.REQUIRES_NEW)
    public List<Date> queryDistinctDate(){
        return marketInfoNewMapper.queryDistinctDate();
    }

    /**
     * 处理单个股票的下载任务
     */
    private boolean queryInsert(ShareInfo excelShare) throws Exception {
        Date date = excelShare.getHqUpdateTime();
        MarketInputDomain inputDomain = new MarketInputDomain();
        String shareCode = excelShare.getShareCode();
        inputDomain.setShareCode(shareCode);
        if (date == null) {
            inputDomain.setStartTime(DateUtils.operateDate(new Date(), -3000, DateUtils.DateFormat.YYYYMMDD.getFormat()));
        } else {
            inputDomain.setStartTime(DateUtils.operateDate(date, 1, DateUtils.DateFormat.YYYYMMDD.getFormat()));
        }
        inputDomain.setEndTime(DateUtils.format(new Date(), DateUtils.DateFormat.YYYYMMDD));
        if (inputDomain.getEndTime().compareTo(inputDomain.getStartTime()) < 0) {
            log.debug("股票 {} 无需更新数据", shareCode);
            return true;
        }
        // 增加重试次数从 3 到 5
        SockerSouhuImportEntity importEntity = this.getMarketJsonBySouhuWithRetry(inputDomain, 5);
        if (importEntity == null) {
            log.warn("股票 {} 获取数据失败", shareCode);
            return false;
        }
        log.info("股票 {} 成功获取到 {} 条数据", shareCode,
                importEntity.getList() != null ? importEntity.getList().size() : 0);
        List<MarketInfoNew> list = importEntity.getList();
        if (list != null && list.size() > 0) {
            marketInfoNewMapper.insertOrUpdateMarketInfo(list);
            // 更新行情时间 - 使用纯股票代码（去除 cn_ 前缀）
            String marketShareCode = list.get(0).getShareCode();
            if (marketShareCode != null && marketShareCode.startsWith("cn_")) {
                marketShareCode = marketShareCode.substring(3);
            }
            log.info("股票 {} 准备更新 hqUpdateTime 为 {}", shareCode, inputDomain.getEndTime());
            sharesInfoService.updateHqUpdateTimeByShareCode(shareCode, inputDomain.getEndTime());
            log.info("股票 {} 插入 {} 条行情数据成功，hqUpdateTime 已更新", shareCode, list.size());
        }
        return true;
    }

    private List<String> getInfoByParam(MarketInputDomain inputDomain) throws IOException {
        List<String> list = new ArrayList<>();
        //有的股票是前缀加 0 有的股票前缀加 1
        String[] array = {"0", "1"};
        for (int i = array.length - 1; i >= 0; i--) {
            if (list != null && list.size() > 1) {
                return new ArrayList<>();
            }
            // 参数
            StringBuffer params = new StringBuffer();
            // 字符数据最好 encoding 以下;这样一来，某些特殊字符才能传过去 (如：某人的名字就是"&",不 encoding 的话，传不过去)
            if ("A00001".equals(inputDomain.getShareCode())) {
                params.append("code=0000001");
            } else {
                params.append("code=" + array[i] + inputDomain.getShareCode());
            }
            params.append("&");
            params.append("start=" + inputDomain.getStartTime().replace("-", ""));
            params.append("&");
            params.append("end=" + inputDomain.getEndTime().replace("-", ""));
            // 创建 Get 请求
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
        // 创建 Get 请求
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
            socker.setOpenValue(MathConstants.ParseStrPointKeep(list.get(1), 4));
            socker.setEndValue(MathConstants.ParseStrPointKeep(list.get(2), 4));
            socker.setRiseFall(MathConstants.ParseStrPointKeep(list.get(3), 4));
            if (list.get(4).contains("%")) {
                socker.setRiseFallRatio(MathConstants.ParseStrPointKeep(list.get(4).replace("%", ""), 4));
            }
            socker.setRiseFallRatioStr(list.get(4));
            socker.setLowest(MathConstants.ParseStrPointKeep(list.get(5), 4));
            socker.setHighest(MathConstants.ParseStrPointKeep(list.get(6), 4));
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

    private static final Random RANDOM = new Random();

    // 信号量限制并发请求数，避免被搜狐限流（优化：从 30 增加到 40）
    private static final java.util.concurrent.Semaphore RATE_LIMITER = new java.util.concurrent.Semaphore(40);

    /**
     * 带重试机制的搜狐行情获取方法（优化版 - 高成功率 + 性能平衡）
     * @param inputDomain 输入参数
     * @param maxRetries 最大重试次数
     * @return 行情数据实体
     */
    public SockerSouhuImportEntity getMarketJsonBySouhuWithRetry(MarketInputDomain inputDomain, int maxRetries) throws Exception {
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
        if (StringUtils.isEmpty(period)){
            periodUrl = "&period=d";
        }else {
            periodUrl = "&period="+period;
        }
        String totalUrl = sohuUrl + params + soHuStaticParam+ periodUrl;

        int retryCount = 0;
        HttpClientUtils.HttpResponseResult responseResult = null;

        while (retryCount < maxRetries) {
            java.util.concurrent.Semaphore semaphore = RATE_LIMITER;
            boolean acquired = false;
            try {
                // 获取信号量许可，最多等待 30 秒（增加等待时间）
                acquired = semaphore.tryAcquire(30, TimeUnit.SECONDS);
                if (!acquired) {
                    log.warn("股票 {} 等待信号量超时，跳过本次请求", inputDomain.getShareCode());
                    return null;
                }

                // 直接调用底层方法
                responseResult = HttpClientUtils.getXpathByHttpClient(totalUrl);

                if (responseResult.isSuccess()) {
                    String response = responseResult.getContent();
                    if (! "{}".equals(response)) {
                        JSONArray array = JSONArray.parseArray(response);
                        if (array != null && array.size() > 0) {
                            SockerSouhuImportEntity entity = array.getObject(0, SockerSouhuImportEntity.class);
                            List<List<String>> hqList = entity.getHq();

                            if (!CollectionUtils.isEmpty(hqList)) {
                                log.debug("股票 {} 获取成功，数据量：{}", inputDomain.getShareCode(), hqList.size());

                                String code = entity.getCode();
                                List<MarketInfoNew> sohuList = new ArrayList<>();
                                for (List<String> list : hqList) {
                                    MarketInfoNew socker = new MarketInfoNew();
                                    socker.setDate(DateUtils.parse(list.get(0), DateUtils.DateFormat.YYYY_MM_DD));
                                    socker.setShareCode(code);
                                    socker.setOpenValue(MathConstants.ParseStrPointKeep(list.get(1), 4));
                                    socker.setEndValue(MathConstants.ParseStrPointKeep(list.get(2), 4));
                                    socker.setRiseFall(MathConstants.ParseStrPointKeep(list.get(3), 4));
                                    if (list.get(4).contains("%")) {
                                        socker.setRiseFallRatio(MathConstants.ParseStrPointKeep(list.get(4).replace("%", ""), 4));
                                    }
                                    socker.setRiseFallRatioStr(list.get(4));
                                    socker.setLowest(MathConstants.ParseStrPointKeep(list.get(5), 4));
                                    socker.setHighest(MathConstants.ParseStrPointKeep(list.get(6), 4));
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
                        }
                    }
                }

                retryCount++;
                if (retryCount < maxRetries) {
                    // 优化：中等延迟策略 - 平衡成功率和性能
                    // 503 错误：1s, 2s, 4s, 8s + 随机 0-500ms
                    // 其他错误：0.5s, 1s, 2s, 4s + 随机 0-300ms
                    boolean is503 = responseResult != null && responseResult.is503();
                    long baseDelay = is503 ? 1000L : 500L;
                    long delay = baseDelay * (1L << (retryCount - 1)) + RANDOM.nextInt(is503 ? 500 : 300);

                    if (is503) {
                        log.warn("股票 {} 第 {} 次请求返回 503，{}ms 后重试", inputDomain.getShareCode(), retryCount, delay);
                    } else {
                        log.debug("股票 {} 第 {} 次请求返回无效数据，{}ms 后重试", inputDomain.getShareCode(), retryCount, delay);
                    }
                    Thread.sleep(delay);
                }

            } catch (Exception e) {
                retryCount++;
                if (retryCount < maxRetries) {
                    // 中等延迟：500ms, 1s, 2s, 4s + 随机 0-300ms
                    long delay = 500L * (1L << (retryCount - 1)) + RANDOM.nextInt(300);
                    log.debug("股票 {} 第 {} 次请求失败：{}，{}ms 后重试", inputDomain.getShareCode(), retryCount - 1, e.getMessage(), delay);
                    Thread.sleep(delay);
                }
            } finally {
                if (acquired) {
                    semaphore.release();
                }
            }
        }

        if (maxRetries > 0) {
            log.error("股票 {} 经过 {} 次重试后仍然失败", inputDomain.getShareCode(), maxRetries);
        }
        return null;
    }

    @Override
    public void runMarketDataInfo(DownloadMarketInputDTO downloadMarketInputDTO) {
        try {
            //跑行情数据
            getMarketInfoBySouHu(downloadMarketInputDTO);
            //跑收益率数据
            QueryRecentSerialRedConditionDTO queryRecentSerialRedConditionDTO = new QueryRecentSerialRedConditionDTO();
            queryRecentSerialRedConditionDTO.setAllFlag(true);
            tradeMethodService.queryThreeDownRatioByDate(queryRecentSerialRedConditionDTO);
            //过滤出符合购买条件的数据到 condition 表中
            QueryProfitByComProgram queryProfitByComProgram = new QueryProfitByComProgram();
            BeanUtils.copyProperties(downloadMarketInputDTO,queryProfitByComProgram);
            tradeInfoService.getFixSerialDown(queryProfitByComProgram);

        } catch (Exception e) {
            log.error("跑行情失败:{}",e);
        }
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


    /**
     * 下载任务线程
     */
    class PatchThread implements Runnable{

        ShareInfo excelShare;
        List<String> failedShares;
        CountDownLatch latch;

        public void setExcelShare(ShareInfo excelShare) {
            this.excelShare = excelShare;
        }

        public ShareInfo getExcelShare() {
            return excelShare;
        }
        PatchThread(ShareInfo shareInfo, List<String> failedShares, CountDownLatch latch){
            excelShare = shareInfo;
            this.failedShares = failedShares;
            this.latch = latch;
        }


        @Override
        public void run() {
            try {
                boolean success = queryInsert(excelShare);
                if (!success) {
                    synchronized (failedShares) {
                        failedShares.add(excelShare.getShareCode());
                    }
                    log.warn("股票 {} 下载失败，已加入失败列表", excelShare.getShareCode());
                }
            } catch (Exception e) {
                synchronized (failedShares) {
                    failedShares.add(excelShare.getShareCode());
                }
                log.error("股票 {} 下载异常：{}", excelShare.getShareCode(), e.getMessage());
            } finally {
                // 任务完成，计数器减 1
                latch.countDown();
            }
        }
    }

    // 记录失败的股票列表，用于重试
    private static List<String> failedShareCodes = new ArrayList<>();

    public static List<String> getFailedShareCodes() {
        return failedShareCodes;
    }

    class RetryThread implements Runnable {
        ShareInfo excelShare;
        List<String> stillFailedShares;

        RetryThread(ShareInfo shareInfo, List<String> stillFailedShares) {
            this.excelShare = shareInfo;
            this.stillFailedShares = stillFailedShares;
        }

        @Override
        public void run() {
            try {
                boolean success = queryInsert(excelShare);
                if (!success) {
                    synchronized (stillFailedShares) {
                        stillFailedShares.add(excelShare.getShareCode());
                    }
                    log.warn("重试股票 {} 仍然失败", excelShare.getShareCode());
                }
            } catch (Exception e) {
                synchronized (stillFailedShares) {
                    stillFailedShares.add(excelShare.getShareCode());
                }
                log.error("重试股票 {} 异常：{}", excelShare.getShareCode(), e.getMessage());
            }
        }
    }
}
