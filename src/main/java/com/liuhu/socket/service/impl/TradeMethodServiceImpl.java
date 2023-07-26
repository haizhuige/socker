package com.liuhu.socket.service.impl;

import com.liuhu.socket.common.DateUtils;
import com.liuhu.socket.dao.MarketInfoNewMapper;
import com.liuhu.socket.dao.SerialTempMapper;
import com.liuhu.socket.domain.input.GetRateThreeIncomeInputDTO;
import com.liuhu.socket.domain.input.QueryRecentSerialRedConditionDTO;
import com.liuhu.socket.domain.output.MarketRateTheeOutPutDTO;
import com.liuhu.socket.domain.output.QueryRecentSerialRedOutPutDTO;
import com.liuhu.socket.dto.QueryRecentSerialRedConditionDO;
import com.liuhu.socket.dto.Xia2Shang1InnerDTO;
import com.liuhu.socket.entity.MarketInfoNew;
import com.liuhu.socket.service.TradeInfoService;
import com.liuhu.socket.service.TradeMethodService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Service("moRen")
public class TradeMethodServiceImpl  implements TradeMethodService {

    @Resource
    SerialTempMapper serialTempMapper;

    @Resource
    TradeInfoService tradeInfoService;

    @Resource
    MarketInfoNewMapper marketInfoNewMapper;


    /**
     * 执行如果连red三次则结束
     * @param input2Domain
     * @return
     */
    @Override
    public List<QueryRecentSerialRedOutPutDTO> getRecentFinalRatioStrategy(QueryRecentSerialRedConditionDTO input2Domain){
        QueryRecentSerialRedConditionDO queryRecentSerialRedConditionDO = new QueryRecentSerialRedConditionDO();
        queryRecentSerialRedConditionDO.setSelectStartTime(DateUtils.parse(input2Domain.getSelectStartTime(), DateUtils.DateFormat.YYYY_MM_DD_HH_MM_SS));
        queryRecentSerialRedConditionDO.setSelectEndTime(DateUtils.parse(input2Domain.getSelectEndTime(), DateUtils.DateFormat.YYYY_MM_DD_HH_MM_SS));
        BeanUtils.copyProperties(input2Domain,queryRecentSerialRedConditionDO);
         List<QueryRecentSerialRedOutPutDTO> outPutDTOList = serialTempMapper.getResultByCondition(queryRecentSerialRedConditionDO);
         if (outPutDTOList.size()==0){
             return null;
         }
         List<QueryRecentSerialRedOutPutDTO>  redThreeList  =  serialTempMapper.getRecentFinalRatioRedThree(outPutDTOList,queryRecentSerialRedConditionDO);

         serialTempMapper.insertList(redThreeList,10);

         return redThreeList;
     }

    @Override
    public List<QueryRecentSerialRedOutPutDTO> queryVRatioFromDownStartPoint(QueryRecentSerialRedConditionDTO input2Domain) throws Exception {

        ExecutorService executorService = Executors.newFixedThreadPool(10);
        String startTime = input2Domain.getStartTime();
        if (Objects.isNull(startTime)) {
            throw new Exception("截至日期不能为空");
        }
        List<QueryRecentSerialRedOutPutDTO> allInfoList = new ArrayList<>();
        /**
         * 分别查询上涨区间内可能完成的交易日查询
         */
        //获取需要查询的日期集合
        List<Date> dateList = tradeInfoService.queryPeriodDateList(startTime, input2Domain.getPeriod(),"sub");
        int totalSize = dateList.size();
        int partSize = (int) Math.ceil((double) totalSize / 10);



        List<Future<List<QueryRecentSerialRedOutPutDTO>>> list = new ArrayList<>();
        for (int i = 0; i < totalSize; i += partSize) {
            int endIndex = Math.min(i + partSize, totalSize);
            List<Date> sublist = dateList.subList(i, endIndex);
            MyTask myTask = new MyTask(sublist,input2Domain);
            Future<List<QueryRecentSerialRedOutPutDTO>> submit = executorService.submit(myTask);
            list.add(submit);
            // result.add(sublist);
        }

        for (Future<List<QueryRecentSerialRedOutPutDTO>> future : list) {
            try {
                // 调用get方法获取任务结果，如果任务还未完成，get方法会阻塞直到任务完成
                List<QueryRecentSerialRedOutPutDTO> subList= future.get();
                allInfoList.addAll(subList);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        executorService.shutdown();

        if (allInfoList.size()>0){
            serialTempMapper.insertList(allInfoList,input2Domain.getRateOrAmountDay());
        }

        return allInfoList;
    }

    @Override
    public List<QueryRecentSerialRedOutPutDTO> queryThreeDownRatioByDate(QueryRecentSerialRedConditionDTO input2Domain) throws Exception {

        String startTime = input2Domain.getStartTime();
        if (Objects.isNull(startTime)) {
            throw new Exception("截至日期不能为空");
        }
        List<QueryRecentSerialRedOutPutDTO> allInfoList = new ArrayList<>();
        /**
         * 分别查询上涨区间内可能完成的交易日查询
         */
        //获取需要查询的日期集合
        List<Date> dateList = tradeInfoService.queryPeriodDateList(startTime, input2Domain.getPeriod(),"sub");
       // List<QueryRecentSerialRedOutPutDTO> allInfoList = new ArrayList<>();

        for (Date date:dateList){
<<<<<<< HEAD

          //  List<MarketInfoNew> marketInfoNewList = marketInfoNewMapper.queryMarketInfoByDate(date);
            List<MarketInfoNew> marketInfoNewList = marketInfoNewMapper.querySerialRedFiveInfoByDate(date,input2Domain.getMinUpDay());
=======
            input2Domain.setStartTime(DateUtils.format(date,DateUtils.DateFormat.YYYY_MM_DD_HH_MM_SS));
            List<MarketInfoNew> marketInfoNewList = marketInfoNewMapper.queryMarketInfoByDate(input2Domain);
>>>>>>> ff97b54086fafb12515bab23441496b27da349f0

            if (marketInfoNewList.size()==0){
                continue;
            }
            List<Date> selectDateList = marketInfoNewList.stream().map(marketInfoNew -> marketInfoNew.getDate()).collect(Collectors.toList());
            Date newDate = selectDateList.get(0);
             newDate = tradeInfoService.getWantDate(1, newDate, "plus");
            List<String> shareCodeList = marketInfoNewList.stream().map(marketInfoNew -> marketInfoNew.getShareCode()).collect(Collectors.toList());
            List<QueryRecentSerialRedOutPutDTO> outPutDTOList =  marketInfoNewMapper.queryThreeDownThen(newDate,shareCodeList);
            allInfoList.addAll(outPutDTOList);
        }
        if (allInfoList.size()>0){
            serialTempMapper.insertList(allInfoList,input2Domain.getRateOrAmountDay());
        }

        return null;
    }

    @Override
    public MarketRateTheeOutPutDTO getRateThreeIncome(GetRateThreeIncomeInputDTO getRateThreeIncomeInputDTO) {
        MarketRateTheeOutPutDTO returnRateDTO = new MarketRateTheeOutPutDTO();
        List<QueryRecentSerialRedOutPutDTO> minRateThreeList = serialTempMapper.getMinRateThree(getRateThreeIncomeInputDTO);
        if (minRateThreeList.size() == 0) {
            return returnRateDTO;
        }
        Double income = 0.0;

        Map<Date,List<Xia2Shang1InnerDTO>> innerMap = new HashMap<>();
        List<Xia2Shang1InnerDTO> innerDTOList = new ArrayList<>();
        for (QueryRecentSerialRedOutPutDTO queryRecentSerialRedOutPutDTO : minRateThreeList) {
            Date startTime = queryRecentSerialRedOutPutDTO.getStartTime();
            Double maxRatio = queryRecentSerialRedOutPutDTO.getMaxRatio();
            Double finalRatio = queryRecentSerialRedOutPutDTO.getFinalRatio();

            Xia2Shang1InnerDTO xia2Shang1InnerDTO = new Xia2Shang1InnerDTO();
            xia2Shang1InnerDTO.setStartTime(startTime);
            xia2Shang1InnerDTO.setShareCode(queryRecentSerialRedOutPutDTO.getShareCode());
            xia2Shang1InnerDTO.setPurchaseUnitCount(1);
            if (maxRatio>4){
                income = income +4;
                xia2Shang1InnerDTO.setEndTime(startTime);
                xia2Shang1InnerDTO.setIncome(4D);
            }else if (finalRatio>0){
                income = income +finalRatio;
                xia2Shang1InnerDTO.setEndTime(startTime);
                xia2Shang1InnerDTO.setIncome(finalRatio);
            }else if (finalRatio<0){
                //当天收益计入总收益
                income = income +finalRatio;
                xia2Shang1InnerDTO.setIncome(finalRatio);
            }
            innerDTOList.add(xia2Shang1InnerDTO);
            //过滤出之前还没有回本得geGu,且endTime为空得。(如果未回本  endTime 为空)
            List<Xia2Shang1InnerDTO> filterList = innerDTOList.stream().filter(xia2Shang1InnerDTO1 -> startTime.compareTo(xia2Shang1InnerDTO1.getStartTime())>0&&xia2Shang1InnerDTO1.getEndTime()==null).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(filterList)){
                continue;
            }
            innerDTOList.removeAll(filterList);
            //遍历那些未回本得geGu集合
            for (Xia2Shang1InnerDTO xia2Shang1InnerDTO2:filterList){
                //获取还没有回本得geGu收益
                Double personalIncome = xia2Shang1InnerDTO2.getIncome();
                //设置购买数量,当日结束计入当天购买数量
                //如果赚到1%的相应的倍数则设置endTime;终止参与循环
                int i = Math.abs((int)(xia2Shang1InnerDTO2.getIncome() / 20))+1;
                xia2Shang1InnerDTO2.setPurchaseUnitCount(i);
                if (i*maxRatio+personalIncome>i){
                    xia2Shang1InnerDTO2.setEndTime(startTime);
                    xia2Shang1InnerDTO2.setIncome((double) i);
                    income = income - personalIncome+i;
                }else {
                    xia2Shang1InnerDTO2.setIncome(xia2Shang1InnerDTO2.getIncome()+finalRatio);
                    income = income +i*finalRatio;
                }
            }
            innerDTOList.addAll(filterList);
            List<Xia2Shang1InnerDTO> list = new ArrayList<>(filterList);
            innerMap.put(startTime,list);
        }
        int maxCount = -1;
        for (Map.Entry<Date,List<Xia2Shang1InnerDTO>> entry:innerMap.entrySet()){
            List<Xia2Shang1InnerDTO> value = entry.getValue();
            List<Integer> unitCountList = value.stream().map(xia2Shang1InnerDTO -> xia2Shang1InnerDTO.getPurchaseUnitCount()).collect(Collectors.toList());
            Integer sumCount = unitCountList.stream().reduce(0, Integer::sum);
            if (sumCount>maxCount){
                maxCount =sumCount;
            }
        }
        returnRateDTO.setIncome(income);
        returnRateDTO.setMaxCount(maxCount);

        return returnRateDTO;
    }


    List<QueryRecentSerialRedOutPutDTO> insertSerialTemp(QueryRecentSerialRedConditionDTO input2Domain, List<Date> dateList) {

        List<QueryRecentSerialRedOutPutDTO> allInfoList = new ArrayList<>();
        for (Date date : dateList) {
            QueryRecentSerialRedConditionDO queryRecentSerialRedConditionDO = new QueryRecentSerialRedConditionDO();
            BeanUtils.copyProperties(input2Domain,queryRecentSerialRedConditionDO);
            queryRecentSerialRedConditionDO.setDownStartTime(date);
            List<QueryRecentSerialRedOutPutDTO> marketOutputDomains = marketInfoNewMapper.queryVRatioFromDownStartPoint(queryRecentSerialRedConditionDO);
            allInfoList.addAll(marketOutputDomains);
        }

        return allInfoList;

    }






    class MyTask implements Callable<List<QueryRecentSerialRedOutPutDTO>> {
        private List<Date> dateList;

        private QueryRecentSerialRedConditionDTO input2Domain;

        public MyTask(List<Date> dateList,QueryRecentSerialRedConditionDTO input2Domain) {
            this.dateList = dateList;
            this.input2Domain = input2Domain;
        }

        @Override
        public List<QueryRecentSerialRedOutPutDTO> call() throws Exception {
            // 模拟耗时操作
            List<QueryRecentSerialRedOutPutDTO> allInfoList  = insertSerialTemp(input2Domain,dateList);
            return allInfoList;
        }
    }


}


