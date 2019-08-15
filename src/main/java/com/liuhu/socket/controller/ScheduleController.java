package com.liuhu.socket.controller;

import com.liuhu.socket.common.ResponseResult;
import com.liuhu.socket.schedule.MarketScheduleServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.io.IOException;

@Controller
@Slf4j
public class ScheduleController {
    @Resource
    MarketScheduleServiceImpl scheduleTask;
    @ResponseBody
    @RequestMapping("/getSynMarketDate.do")
    public ResponseResult getShareInfo()  {
        try {
            scheduleTask.getNewMarketInfo();
        } catch (IOException e) {
            log.error("导入行情失败{}",e);
            return ResponseResult.done("导入失败");
        }
        return ResponseResult.done("导入成功");
    }
}
