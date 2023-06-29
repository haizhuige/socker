package com.liuhu.socket.test.gc;

/**
 * @Author liuhu-jk
 * @Date 2019/11/1 13:49
 * @Description
 **/
public class HelloGc {
    public static void main(String[] args) {
        long totalMemory =  Runtime.getRuntime().totalMemory();//初始内存为计算机总内存得六十四分之一  243mb

        long maxMemory =Runtime.getRuntime().freeMemory();//虚拟机最大内存

        System.out.println(totalMemory);
        System.out.println(maxMemory);

    }
}
