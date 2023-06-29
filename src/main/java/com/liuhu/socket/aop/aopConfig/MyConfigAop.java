package com.liuhu.socket.aop.aopConfig;

import com.liuhu.socket.aop.aspectMethod.Calculate;
import com.liuhu.socket.aop.logger.AopLogger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * @Author liuhu-jk
 * @Date 2019/11/2 16:13
 * @Description
 **/
@Configuration
@EnableAspectJAutoProxy
public class MyConfigAop {

    @Bean
    public Calculate getMyConfigAop(){
        return new Calculate();
    }

    @Bean
    public AopLogger getLogger(){
        return new AopLogger();
    }

}
