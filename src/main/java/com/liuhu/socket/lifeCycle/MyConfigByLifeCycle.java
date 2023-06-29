package com.liuhu.socket.lifeCycle;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @Author liuhu-jk
 * @Date 2019/11/3 10:30
 * @Description
 **/
@Configuration
@ComponentScan(value = "com.liuhu.socket.lifeCycle")
public class MyConfigByLifeCycle {

    @Bean(initMethod = "initMethod",destroyMethod = "destroyed" )
    public School school(){
       return new School();
   }

   @Bean
    public Person person(){
        return new Person();
   }
}
