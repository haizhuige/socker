package com.liuhu.socket.lifeCycle;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * @Author liuhu-jk
 * @Date 2019/11/3 10:25
 * @Description
 **/
public class School implements InitializingBean, DisposableBean {

    public School() {
        System.out.println("school构造函数。。。。。");
    }

    private Person person;

    public void initMethod(){
        System.out.println("初始化方法。。。。。");
    }
    public void destroyed(){
        System.out.println("销毁方法。。。。。。");
    }
    @PostConstruct
    public void annotationInitMethod(){
        System.out.println("@PostConstruct初始化方法。。。。。");
    }
    @PreDestroy
    public void annotationDestroyed(){
        System.out.println("@PreDestroy销毁方法。。。。。。");
    }


    @Override
    public void destroy() throws Exception {
        System.out.println("destroy.........");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("afterPropertiesSet.....");
    }

    public void setPerson(Person person) {
        this.person = person;
    }

   public Person getPerson() {
        return person;
    }
}
