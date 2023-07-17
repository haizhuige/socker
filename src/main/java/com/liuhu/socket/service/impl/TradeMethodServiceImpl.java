package com.liuhu.socket.service.impl;

import com.liuhu.socket.common.DateUtils;
import com.liuhu.socket.dao.MarketInfoNewMapper;
import com.liuhu.socket.dao.SerialTempMapper;
import com.liuhu.socket.domain.input.QueryRecentSerialRedConditionDTO;
import com.liuhu.socket.domain.output.QueryRecentSerialRedOutPutDTO;
import com.liuhu.socket.dto.QueryRecentSerialRedConditionDO;
import com.liuhu.socket.service.TradeInfoService;
import com.liuhu.socket.service.TradeMethodService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
public class TradeMethodServiceImpl  implements TradeMethodService {

    @Resource
    SerialTempMapper serialTempMapper;

    @Resource
    TradeInfoService tradeInfoService;

    @Resource
    MarketInfoNewMapper marketInfoNewMapper;


    ExecutorService executorService = Executors.newFixedThreadPool(10);


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


