package com.liuhu.socket.test.proxy;

/**
 * @Author liuhu-jk
 * @Date 2019/10/29 17:33
 * @Description
 **/
public class HelloImpl implements HelloInterface {
    @Override
    public void helloWorld() {
        System.out.println("你好，世界！！！");
    }
}
