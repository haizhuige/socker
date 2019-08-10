package com.liuhu.socket.controller;

import com.liuhu.socket.common.ResponseResult;
import com.liuhu.socket.domain.MarketInputDomain;
import com.liuhu.socket.entity.MarketInfo;
import com.liuhu.socket.schedule.ExcelSchedule;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

@Controller
public class SchedualController {
    @Resource
    ExcelSchedule excelTask;
    @ResponseBody
    @RequestMapping("/getSynMarketDate.do")
    public ResponseResult getShareInfo()  {
        try {
            excelTask.getNewMarketInfo();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ResponseResult.done(null);
    }
}
