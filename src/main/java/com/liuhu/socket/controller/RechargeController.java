package com.liuhu.socket.controller;

import com.liuhu.socket.common.ResponseResult;
import com.liuhu.socket.domain.CustomerInputDomain;
import com.liuhu.socket.domain.RechargeInputDomain;
import com.liuhu.socket.service.RechargeInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

/**
 * @Author liuhu-jk
 * @Date 2019/8/15 17:44
 * @Description   用户管理及充值信息
 **/
@Controller
@Slf4j
@RequestMapping("/recharge")
public class RechargeController {
	
	@Resource
    RechargeInfoService rechargeInfoService;
    /**
     * 添加新用户
     * @param input
     * @return
     */
    @ResponseBody
    @RequestMapping("/addUser.do")
    public ResponseResult addUser(@RequestBody CustomerInputDomain input) {

        Integer a = rechargeInfoService.addCustomer(input);
        return ResponseResult.done(a);
    }
	/**
	 * 充值操作
	 * @param input
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/addRecharge.do")
	public ResponseResult operateTrade(@RequestBody RechargeInputDomain input) {
	    if (input==null|| StringUtils.isEmpty(input.getPersonId())||input.getAmount()<=0){
	        return ResponseResult.failed("充值入参为空");
        }
        Integer a = rechargeInfoService.addRecharge(input);
		return ResponseResult.done(a);
	}
}
