package com.liuhu.socket.test.queue;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author liuhu-jk
 * @Date 2019/10/31 10:35
 * @Description
 **/
public class BlockQueueTest {
    BlockingQueue<Integer> linkedBlockingQueue = null;
    AtomicInteger atomicInteger = new AtomicInteger();
    volatile   boolean flag = true;
    public BlockQueueTest(BlockingQueue<Integer> linkedBlockingQueue) {
        this.linkedBlockingQueue = linkedBlockingQueue;
        System.out.println("线程"+Thread.currentThread().getName());
    }

    public void consumer() throws InterruptedException {
         while (flag){
            int a =  linkedBlockingQueue.poll(2,TimeUnit.SECONDS);
            System.out.println("获取的数据剩余"+a);
             TimeUnit.SECONDS.sleep(1);
         }
    }
    public void product() throws InterruptedException {
         while (flag){
           int a=   atomicInteger.incrementAndGet();
           boolean offerFlag  =  linkedBlockingQueue.offer(a,2, TimeUnit.SECONDS);
           if (offerFlag){
               System.out.println("插入成功");
           }else{
               System.out.println("插入失败");
           }
           TimeUnit.SECONDS.sleep(1);
         }
    }

    public void changeFlag(){
        flag = false;
    }

    public static void main(String[] args) {

        BlockQueueTest blockQueueTest = new BlockQueueTest(new ArrayBlockingQueue<>(5));
         new Thread(()->{
             try {
                 blockQueueTest.product();
             } catch (InterruptedException e) {
                 e.printStackTrace();
             }
         }).start();
        new Thread(()->{
            try {
                blockQueueTest.consumer();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("等5秒钟停止");
        blockQueueTest.changeFlag();
    }
}
