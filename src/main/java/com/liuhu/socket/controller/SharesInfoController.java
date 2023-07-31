package com.liuhu.socket.controller;

import com.liuhu.socket.common.ResponseResult;
import com.liuhu.socket.domain.input.DownloadMarketInputDTO;
import com.liuhu.socket.entity.ShareInfo;
import com.liuhu.socket.schedule.MarketScheduleServiceImpl;
import com.liuhu.socket.service.SharesInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

@Controller
@Slf4j
public class SharesInfoController {
    @Resource
    MarketScheduleServiceImpl scheduleTask;

    @Resource
    SharesInfoService sharesInfoService;

    @ResponseBody
    @RequestMapping("/getSynMarketDate.do")
    public ResponseResult getSynMarketDate(@RequestBody DownloadMarketInputDTO downloadMarketInputDTO) {
        try {
            scheduleTask.getMarketInfoBySouHu(downloadMarketInputDTO);
        } catch (IOException e) {
            log.error("导入行情失败{}", e);
            return ResponseResult.done("导入失败");
        }
        return ResponseResult.done("导入成功");
    }

    @ResponseBody
    @RequestMapping("/getShareNameAndCode.do")
    public ResponseResult getShareInfo() {
     //   List<ShareInfo> list = sharesInfoService.getShareInfo();
        return ResponseResult.done("nimei");
    }
}
