package com.liuhu.socket.test;

/**
 * @Author liuhu-jk
 * @Date 2019/10/23 9:58
 * @Description
 **/
public class ParentOrder {

    static{
        System.out.println("父类静态代码块");
    }
    {
        System.out.println("父类构造代码块");
    }
    public ParentOrder(){
        System.out.println("父类构造方法");
    }


}
