package com.liuhu.socket.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.liuhu.socket.common.ResponseResult;
import com.liuhu.socket.dao.ShareInfoMapper;
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
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Controller
@Slf4j
public class SharesInfoController {
    @Resource
    MarketScheduleServiceImpl scheduleTask;

    @Resource
    ShareInfoMapper shareInfoMapper;

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

    @ResponseBody
    @RequestMapping("/addShareCodeAndName")
    public ResponseResult addShareCodeAndName(MultipartFile file) {
        String    content = null;
        try {
            content = new String(file.getBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }

        JSONObject jsonObject1 = JSONObject.parseObject(content);
        JSONObject json = (JSONObject) jsonObject1.get("rows");
        JSONArray jsonArray = (JSONArray) json.get("item");
        for (Object obj:jsonArray){
            ShareInfo shareInfo = new ShareInfo();
            JSONObject jobs = (JSONObject) obj;
            String name = (String)jobs.get("name");
            String code = (String)jobs.get("code");
            shareInfo.setHushenStatus("B");
            shareInfo.setShareCode(code);
            shareInfo.setShareName(name);
            shareInfo.setStatus("1");
            shareInfoMapper.insert(shareInfo);
        }
        return ResponseResult.done("设置成功");
    }
}
