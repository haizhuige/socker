package com.liuhu.socket.lifeCycle;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

/**
 * @Author liuhu-jk
 * @Date 2019/11/8 18:12
 * @Description
 **/
@Component
public class MyBeanPostProfessor implements BeanPostProcessor {

    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        System.out.println("BeanPostProcessor初始化之前操作。。。。。。。"+beanName+"=>"+bean);
        return bean;
    }

    public  Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        System.out.println("BeanPostProcessor初始化之后操作。。。。。。。"+beanName+"=>"+bean);
        return bean;
    }
}
