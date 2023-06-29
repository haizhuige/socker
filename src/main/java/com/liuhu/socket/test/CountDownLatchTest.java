package com.liuhu.socket.test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicStampedReference;

/**
 * @Author liuhu-jk
 * @Date 2019/10/17 10:44
 * @Description
 **/
public class CountDownLatchTest{
  static    CountDownLatch countDownLatch = new CountDownLatch(3);

    public static void main(String[] args) throws Exception {
        countdownLatch();

    }

    private static void countdownLatch() {
        AtomicInteger aci = new AtomicInteger(5);
        aci.compareAndSet(5, 100);
        AtomicReference<Person> reference = new AtomicReference<>();
        System.out.println(aci.get());
        AtomicStampedReference<Integer> stampedReference = new AtomicStampedReference<Integer>(10,1);
    }
}
