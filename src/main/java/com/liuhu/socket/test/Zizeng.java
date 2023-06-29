package com.liuhu.socket.test;

/**
 * @Author liuhu-jk
 * @Date 2019/11/18 11:08
 * @Description
 **/
public class Zizeng  {
    public static void main(String[] args) {
        int i =1;
        i=i++;
        int j = i++;
        int k = i+ ++i*i++;
        System.out.println(i);
        System.out.println(j);
        System.out.println(k);
    }
}
