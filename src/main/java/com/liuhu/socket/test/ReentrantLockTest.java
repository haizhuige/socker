package com.liuhu.socket.test;

import sun.misc.VM;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author liuhu-jk
 * @Date 2019/10/22 21:35
 * @Description
 **/
public class ReentrantLockTest {

    public synchronized static void sendSms() {
        System.out.println(Thread.currentThread().getId() + "发送短信");
        sendEmail();
    }

    public synchronized static void sendEmail() {
        System.out.println(Thread.currentThread().getId() + "发送邮件");
    }

    private String getName() {
        System.out.println("私有方法");
        return "siyou";
    }

    public static void main(String[] args) {
        //  AtomicInteger aci = new AtomicInteger();
        for (int i = 0; ; i++) {
            System.out.println("i=" + i);
            new Thread(() -> {
                try {
                    Thread.sleep(Integer.MAX_VALUE);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

}
