package com.liuhu.socket.test;
/**
 * @Author liuhu-jk
 * @Date 2019/10/23 9:55
 * @Description
 **/
public class CodeExcutorOrder extends ParentOrder{
    static{
        System.out.println("静态代码块");
    }
    {
        System.out.println("构造代码块");
    }
    public CodeExcutorOrder(){
        System.out.println("构造方法");
    }

    public static void stg1(){
        System.out.println("静态方法");
    }

    public static void main(String[] args) {
       // CodeExcutorOrder.stg1();
       new CodeExcutorOrder();
    }
}
