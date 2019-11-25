package com.liuhu.socket.aop.aspectMethod;

import com.liuhu.socket.aop.aopConfig.DefAutowired;
import org.springframework.context.annotation.Bean;

/**
 * @Author liuhu-jk
 * @Date 2019/11/2 16:14
 * @Description
 **/
public class Calculate {
    @DefAutowired
    public int multiply(int a,int b){
        System.out.println("multiply结果是"+a*b);
        return a*b;
    }
}
