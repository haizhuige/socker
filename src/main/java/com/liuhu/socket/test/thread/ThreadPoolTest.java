package com.liuhu.socket.test.thread;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Author liuhu-jk
 * @Date 2019/11/5 10:28
 * @Description
 **/
public class ThreadPoolTest {

    public static void main(String[] args) {
        ExecutorService executorService = new ThreadPoolExecutor(10,20,1, TimeUnit.SECONDS,new ArrayBlockingQueue<>(10));

        executorService.execute(new Runnable() {
            @Override
            public void run() {

            }
        });
    }
}
