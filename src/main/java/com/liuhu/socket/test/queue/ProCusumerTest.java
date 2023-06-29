package com.liuhu.socket.test.queue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @Author liuhu-jk
 * @Date 2019/10/31 15:20
 * @Description
 **/
public class ProCusumerTest {
    ReentrantLock lock = new ReentrantLock();
    Condition condition =lock.newCondition();
    AtomicInteger atomicInteger = new AtomicInteger();
    static int maxProduct =5;


    public void product(){
        lock.lock();

            try {
                while (atomicInteger.get()==maxProduct) {
                    System.out.println("集合中已经达到最大值，不能再存值了");
                    condition.await();

                }
                    atomicInteger.incrementAndGet();
                    System.out.println(Thread.currentThread().getName()+"存储值");
                    condition.signalAll();

            } catch (InterruptedException e) {
                e.printStackTrace();
            }finally {
                lock.unlock();
            }
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }
    public void consumer(){
        lock.lock();
            try {
                while (atomicInteger.get()==0) {
                    condition.await();
                    System.out.println("集合中的数据为空不能再拿值了");

                }
                    atomicInteger.decrementAndGet();
                    condition.signalAll();
                    System.out.println(Thread.currentThread().getName()+"消费值还剩余"+atomicInteger.get());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }finally {
                lock.unlock();
            }
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        ProCusumerTest proCusumerTest = new ProCusumerTest();
         new Thread(()->{
             for (int i=1;i<=12;i++){
                 proCusumerTest.product();
             }
         }).start();
        new Thread(()->{
            for (int i=1;i<=12;i++){
                proCusumerTest.consumer();
            }
        }).start();
    }
}
